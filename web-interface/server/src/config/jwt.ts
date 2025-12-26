export const jwtConfig = {
  accessTokenSecret: process.env.JWT_SECRET || 'change-this-secret',
  refreshTokenSecret: process.env.JWT_REFRESH_SECRET || 'change-this-refresh-secret',
  accessTokenExpiry: '30d', // 30 days
  refreshTokenExpiry: '90d', // 90 days
};
