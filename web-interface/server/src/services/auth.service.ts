import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import { v4 as uuidv4 } from 'uuid';
import { AppDataSource } from '../config/database';
import { jwtConfig } from '../config/jwt';
import { User } from '../models/User';
import { RefreshToken } from '../models/RefreshToken';
import { SyncState } from '../models/SyncState';

const BCRYPT_ROUNDS = 12;

export interface RegisterParams {
  username: string;
  password: string;
  deviceId: string;
}

export interface LoginParams {
  username: string;
  password: string;
  deviceId: string;
}

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
}

export class AuthService {
  private userRepository = AppDataSource.getRepository(User);
  private refreshTokenRepository = AppDataSource.getRepository(RefreshToken);
  private syncStateRepository = AppDataSource.getRepository(SyncState);

  async register(params: RegisterParams): Promise<{ userId: string; message: string }> {
    const { username, password, deviceId } = params;

    // Check if user already exists
    const existingUser = await this.userRepository.findOne({ where: { username } });
    if (existingUser) {
      throw new Error('Username already exists');
    }

    // Check if device is already registered
    const existingDevice = await this.userRepository.findOne({ where: { deviceId } });
    if (existingDevice) {
      throw new Error('Device already registered');
    }

    // Hash password
    const passwordHash = await bcrypt.hash(password, BCRYPT_ROUNDS);

    // Create user
    const user = this.userRepository.create({
      username,
      passwordHash,
      deviceId,
    });

    await this.userRepository.save(user);

    // Create sync state for user
    const syncState = this.syncStateRepository.create({
      userId: user.id,
      syncToken: uuidv4(),
    });
    await this.syncStateRepository.save(syncState);

    return {
      userId: user.id,
      message: 'User created successfully',
    };
  }

  async login(params: LoginParams, isWebBrowser: boolean = false): Promise<{ user: User; tokens: TokenPair }> {
    const { username, password, deviceId } = params;

    // Find user
    const user = await this.userRepository.findOne({ where: { username } });
    if (!user) {
      throw new Error('Invalid credentials');
    }

    // For web browsers, allow login without strict deviceId matching
    // For mobile apps (okhttp), enforce deviceId matching
    if (!isWebBrowser && user.deviceId !== deviceId) {
      throw new Error('Device mismatch - please use the registered device');
    }

    // Verify password
    const isPasswordValid = await bcrypt.compare(password, user.passwordHash);
    if (!isPasswordValid) {
      throw new Error('Invalid credentials');
    }

    // Update last login
    user.lastLogin = new Date();
    await this.userRepository.save(user);

    // Generate tokens
    const tokens = await this.generateTokenPair(user.id);

    return { user, tokens };
  }

  async refreshAccessToken(refreshTokenString: string): Promise<TokenPair> {
    // Verify refresh token
    let payload: any;
    try {
      payload = jwt.verify(refreshTokenString, jwtConfig.refreshTokenSecret);
    } catch (error) {
      throw new Error('Invalid refresh token');
    }

    // Check if token exists in database
    const tokenRecord = await this.refreshTokenRepository.findOne({
      where: { token: refreshTokenString },
    });

    if (!tokenRecord) {
      throw new Error('Refresh token not found');
    }

    // Check if token is expired
    if (new Date() > tokenRecord.expiresAt) {
      await this.refreshTokenRepository.remove(tokenRecord);
      throw new Error('Refresh token expired');
    }

    // Generate new token pair
    const newTokens = await this.generateTokenPair(payload.userId);

    // Remove old refresh token
    await this.refreshTokenRepository.remove(tokenRecord);

    return newTokens;
  }

  async logout(refreshTokenString: string): Promise<void> {
    const tokenRecord = await this.refreshTokenRepository.findOne({
      where: { token: refreshTokenString },
    });

    if (tokenRecord) {
      await this.refreshTokenRepository.remove(tokenRecord);
    }
  }

  async verifyAccessToken(token: string): Promise<{ userId: string }> {
    try {
      const payload = jwt.verify(token, jwtConfig.accessTokenSecret) as any;
      return { userId: payload.userId };
    } catch (error) {
      throw new Error('Invalid access token');
    }
  }

  private async generateTokenPair(userId: string): Promise<TokenPair> {
    // Generate access token
    const accessToken = jwt.sign(
      { userId, type: 'access' },
      jwtConfig.accessTokenSecret,
      { expiresIn: jwtConfig.accessTokenExpiry } as jwt.SignOptions
    );

    // Generate refresh token
    const refreshToken = jwt.sign(
      { userId, type: 'refresh' },
      jwtConfig.refreshTokenSecret,
      { expiresIn: jwtConfig.refreshTokenExpiry } as jwt.SignOptions
    );

    // Store refresh token in database
    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + 90); // 90 days

    const tokenRecord = this.refreshTokenRepository.create({
      userId,
      token: refreshToken,
      expiresAt,
    });

    await this.refreshTokenRepository.save(tokenRecord);

    return { accessToken, refreshToken };
  }
}
