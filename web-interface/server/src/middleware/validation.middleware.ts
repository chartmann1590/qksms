import { Request, Response, NextFunction } from 'express';
import { ValidationError } from '../utils/errors';

// Validation schemas
export const schemas = {
  register: {
    username: {
      required: true,
      minLength: 3,
      maxLength: 50,
      pattern: /^[a-zA-Z0-9_-]+$/,
      message: 'Username must be 3-50 characters and contain only letters, numbers, underscores, and hyphens',
    },
    password: {
      required: true,
      minLength: 8,
      maxLength: 100,
      message: 'Password must be at least 8 characters',
    },
    deviceId: {
      required: true,
      message: 'Device ID is required',
    },
  },
  login: {
    username: {
      required: true,
      message: 'Username is required',
    },
    password: {
      required: true,
      message: 'Password is required',
    },
    deviceId: {
      required: true,
      message: 'Device ID is required',
    },
  },
  sendMessage: {
    addresses: {
      required: true,
      isArray: true,
      minItems: 1,
      message: 'At least one recipient address is required',
    },
    body: {
      required: true,
      maxLength: 10000,
      message: 'Message body is required and must be less than 10000 characters',
    },
  },
};

interface ValidationRule {
  required?: boolean;
  minLength?: number;
  maxLength?: number;
  pattern?: RegExp;
  isArray?: boolean;
  minItems?: number;
  message: string;
}

interface ValidationSchema {
  [key: string]: ValidationRule;
}

/**
 * Validate request body against a schema
 */
export function validate(schema: ValidationSchema) {
  return (req: Request, res: Response, next: NextFunction): void => {
    const errors: string[] = [];

    for (const [field, rules] of Object.entries(schema)) {
      const value = req.body[field];

      // Check required
      if (rules.required && (value === undefined || value === null || value === '')) {
        errors.push(rules.message || `${field} is required`);
        continue;
      }

      // Skip further validation if field is optional and not provided
      if (!rules.required && (value === undefined || value === null)) {
        continue;
      }

      // Check array
      if (rules.isArray && !Array.isArray(value)) {
        errors.push(rules.message || `${field} must be an array`);
        continue;
      }

      if (rules.isArray && Array.isArray(value) && rules.minItems && value.length < rules.minItems) {
        errors.push(rules.message || `${field} must have at least ${rules.minItems} items`);
        continue;
      }

      // Check string validations
      if (typeof value === 'string') {
        if (rules.minLength && value.length < rules.minLength) {
          errors.push(rules.message || `${field} must be at least ${rules.minLength} characters`);
        }

        if (rules.maxLength && value.length > rules.maxLength) {
          errors.push(rules.message || `${field} must be at most ${rules.maxLength} characters`);
        }

        if (rules.pattern && !rules.pattern.test(value)) {
          errors.push(rules.message || `${field} format is invalid`);
        }
      }
    }

    if (errors.length > 0) {
      throw new ValidationError(errors.join(', '));
    }

    next();
  };
}

/**
 * Sanitize string input to prevent XSS
 */
export function sanitizeString(input: string): string {
  if (typeof input !== 'string') return input;

  // Remove HTML tags and dangerous characters
  return input
    .replace(/<script[^>]*>.*?<\/script>/gi, '')
    .replace(/<iframe[^>]*>.*?<\/iframe>/gi, '')
    .replace(/<object[^>]*>.*?<\/object>/gi, '')
    .replace(/<embed[^>]*>/gi, '')
    .trim();
}

/**
 * Sanitize request body
 */
export function sanitizeBody(req: Request, res: Response, next: NextFunction): void {
  if (req.body && typeof req.body === 'object') {
    for (const [key, value] of Object.entries(req.body)) {
      if (typeof value === 'string') {
        req.body[key] = sanitizeString(value);
      }
    }
  }
  next();
}
