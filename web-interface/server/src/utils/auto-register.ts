import { AppDataSource } from '../config/database';
import { User } from '../models/User';
import { SyncState } from '../models/SyncState';
import bcrypt from 'bcrypt';
import { v4 as uuidv4 } from 'uuid';

const BCRYPT_ROUNDS = 12;

/**
 * Auto-register initial user from environment variables if configured
 */
export async function autoRegisterInitialUser(): Promise<void> {
  const username = process.env.INITIAL_USERNAME;
  const password = process.env.INITIAL_PASSWORD;
  const deviceId = process.env.INITIAL_DEVICE_ID;

  // Skip if not configured
  if (!username || !password || !deviceId) {
    console.log('ℹ Auto-registration not configured (INITIAL_USERNAME, INITIAL_PASSWORD, INITIAL_DEVICE_ID not set)');
    return;
  }

  try {
    const userRepository = AppDataSource.getRepository(User);
    const syncStateRepository = AppDataSource.getRepository(SyncState);

    // Check if user already exists
    const existingUser = await userRepository.findOne({ where: { username } });

    if (existingUser) {
      console.log(`✓ User '${username}' already exists, skipping auto-registration`);
      return;
    }

    // Check if device is already registered with another user
    const existingDevice = await userRepository.findOne({ where: { deviceId } });
    if (existingDevice) {
      console.log(`⚠ Device ID '${deviceId}' already registered to user '${existingDevice.username}', skipping auto-registration`);
      return;
    }

    // Hash password
    const passwordHash = await bcrypt.hash(password, BCRYPT_ROUNDS);

    // Create user
    const user = userRepository.create({
      username,
      passwordHash,
      deviceId,
    });

    await userRepository.save(user);

    // Create sync state for user
    const syncState = syncStateRepository.create({
      userId: user.id,
      syncToken: uuidv4(),
    });
    await syncStateRepository.save(syncState);

    console.log(`✓ Auto-registered initial user '${username}' with device ID '${deviceId}'`);
  } catch (error) {
    console.error('✗ Auto-registration failed:', error);
    // Don't throw - server should still start even if auto-registration fails
  }
}
