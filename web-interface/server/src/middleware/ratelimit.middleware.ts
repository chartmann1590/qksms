import rateLimit from 'express-rate-limit';

// General API rate limit: 100 requests per minute
export const apiLimiter = rateLimit({
  windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS || '60000'), // 1 minute
  max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS || '100'),
  message: 'Too many requests from this IP, please try again later.',
  standardHeaders: true,
  legacyHeaders: false,
});

// Rate limit for authentication endpoints: 20 requests per 5 minutes (much more reasonable)
export const authLimiter = rateLimit({
  windowMs: 5 * 60 * 1000, // 5 minutes instead of 1 hour
  max: 20, // 20 attempts instead of 5
  message: 'Too many login attempts, please try again later.',
  standardHeaders: true,
  legacyHeaders: false,
  skipSuccessfulRequests: true, // Don't count successful logins
});
