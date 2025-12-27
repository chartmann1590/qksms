import axios, { AxiosError } from 'axios';
import { Message } from '../models/Message';

export interface OllamaModel {
  name: string;
  size: number;
  modified_at: string;
}

interface OllamaModelsResponse {
  models: OllamaModel[];
}

interface OllamaChatMessage {
  role: 'system' | 'user' | 'assistant';
  content: string;
}

interface OllamaChatRequest {
  model: string;
  messages: OllamaChatMessage[];
  stream: boolean;
  temperature?: number;
  top_p?: number;
  num_predict?: number;
}

interface OllamaChatResponse {
  model: string;
  message: OllamaChatMessage;
  done: boolean;
}

export class OllamaService {
  private readonly TAGS_ENDPOINT = '/api/tags';
  private readonly CHAT_ENDPOINT = '/api/chat';
  private readonly JSON_MEDIA_TYPE = 'application/json; charset=utf-8';

  /**
   * Fetch available models from Ollama server
   */
  async getAvailableModels(baseUrl: string): Promise<OllamaModel[]> {
    try {
      const url = `${baseUrl.trim().replace(/\/$/, '')}${this.TAGS_ENDPOINT}`;
      console.log(`Fetching models from: ${url}`);

      const response = await axios.get<OllamaModelsResponse>(url, {
        timeout: 10000,
      });

      console.log(`Fetched ${response.data.models.length} models`);
      return response.data.models;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError;
        if (axiosError.code === 'ECONNREFUSED' || axiosError.code === 'ETIMEDOUT') {
          throw new Error(`Connection error: ${axiosError.message}`);
        }
        if (axiosError.response) {
          throw new Error(`Failed to fetch models: ${axiosError.response.status} ${axiosError.response.statusText}`);
        }
        throw new Error(`Network error: ${axiosError.message}`);
      }
      throw error;
    }
  }

  /**
   * Generate smart reply suggestions based on conversation context
   */
  async generateReplySuggestions(
    baseUrl: string,
    model: string,
    conversationContext: Message[],
    persona?: string
  ): Promise<string[]> {
    try {
      const url = `${baseUrl.trim().replace(/\/$/, '')}${this.CHAT_ENDPOINT}`;
      console.log(`Generating replies from: ${url} with model: ${model}`);
      console.log(`Received ${conversationContext.length} messages in conversationContext`);

      // Build messages for chat API with system message for strict instructions
      const messages = this.buildChatMessages(conversationContext, persona);
      console.log(`Chat messages count: ${messages.length}`);

      // Create chat request with lower temperature for more focused, less creative responses
      const chatRequest: OllamaChatRequest = {
        model,
        messages,
        stream: false,
        temperature: 0.3, // Lower temperature = more focused, less creative/hallucinatory
        top_p: 0.9, // Nucleus sampling for more focused responses
        num_predict: 200, // Limit response length
      };

      const response = await axios.post<OllamaChatResponse>(url, chatRequest, {
        headers: {
          'Content-Type': this.JSON_MEDIA_TYPE,
        },
        timeout: 30000,
      });

      const responseText = response.data.message.content;

      console.log(`Raw AI response (first 500 chars): ${responseText.substring(0, 500)}`);
      console.log(`Raw AI response length: ${responseText.length}`);

      // Parse the response into multiple suggestions
      const suggestions = this.parseReplySuggestions(responseText);
      console.log(`Generated ${suggestions.length} suggestions: ${suggestions}`);

      return suggestions;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError;
        if (axiosError.code === 'ECONNREFUSED' || axiosError.code === 'ETIMEDOUT') {
          throw new Error(`Connection error: ${axiosError.message}`);
        }
        if (axiosError.response) {
          throw new Error(`Failed to generate replies: ${axiosError.response.status} ${axiosError.response.statusText}`);
        }
        throw new Error(`Network error: ${axiosError.message}`);
      }
      throw error;
    }
  }

  /**
   * Build chat messages for /api/chat endpoint
   * Uses system message for strict instructions and user message with conversation context
   * Each call is a fresh conversation (no shared state between calls)
   */
  private buildChatMessages(messages: Message[], persona?: string): OllamaChatMessage[] {
    const chatMessages: OllamaChatMessage[] = [];

    // System message with strict instructions - this is fresh for each conversation
    const systemPrompt = this.buildSystemPrompt(persona);
    chatMessages.push({ role: 'system', content: systemPrompt });

    // Build conversation context with recent messages
    const conversationContext = this.buildConversationContext(messages);
    chatMessages.push({ role: 'user', content: conversationContext });

    return chatMessages;
  }

  /**
   * Build system prompt with strict instructions
   * Note: Each conversation gets a fresh system message - no shared state between different conversations
   */
  private buildSystemPrompt(persona?: string): string {
    const personaSection = persona && persona.trim() ? `\n${persona}\n` : '';

    return `${personaSection}You are a text message reply suggestion generator. You will be given a conversation context with recent messages, and you need to generate 3-5 short reply suggestions for the MOST RECENT message from the other person.

CRITICAL RULES - YOU MUST FOLLOW THESE:
1. Focus on the MOST RECENT message from the other person (marked as "Them")
2. Use the conversation context to understand what they're responding to, but generate replies ONLY for the most recent message
3. Generate replies that respond to what is explicitly written in the most recent message
4. You can reference topics from the conversation context, but do NOT make up new details not mentioned
5. Do NOT assume context or background information beyond what's in the conversation
6. Do NOT make up details like names, dates, locations, or plans not in the conversation
7. If the message asks a question, answer that question using information from the conversation context
8. If the message makes a statement, respond to that statement appropriately
9. Keep replies short (1-2 sentences), natural, and casual like real text messages
10. Return ONLY numbered suggestions (1-5), one per line, with no explanations or extra text

IMPORTANT: Each request is a NEW conversation - do not reference previous requests or conversations.

EXAMPLES:
Conversation:
Me: "Are you free tomorrow?"
Them: "Yes, I am! What did you have in mind?"

Good replies for "Yes, I am! What did you have in mind?":
1. Great! Want to grab lunch?
2. I was thinking we could go to that new restaurant
3. How about we meet for coffee?

Bad replies (making things up):
1. Great! I'll bring the documents at 3pm (documents and 3pm not mentioned)
2. Perfect! See you at the office (office not mentioned)

Remember: Use the conversation context to understand what's being discussed, but only reference things actually mentioned in the conversation.`;
  }

  /**
   * Build conversation context with recent messages
   * Includes the last 3-5 messages to provide context, focusing on the most recent message from "Them"
   */
  private buildConversationContext(messages: Message[]): string {
    if (messages.length === 0) {
      console.warn('No messages provided for smart reply generation');
      return 'Generate 3-5 short, friendly reply suggestions for a text message conversation.';
    }

    // Filter out messages with empty bodies or just ellipsis
    const validMessages = messages.filter((msg) => {
      const text = (msg.body || '').trim();
      return text.length > 0 && text !== '...' && text.length > 1;
    });

    if (validMessages.length === 0) {
      console.warn('No messages with valid content found (all empty or just ellipsis)');
      return 'Generate 3-5 short, friendly reply suggestions for a text message conversation.';
    }

    // Messages are assumed to be in chronological order (oldest to newest)
    // Get the last 4 valid messages for context
    const recentMessages = validMessages.slice(-4);

    // Debug: Log all messages to see what we're working with
    console.log(`Total messages in context: ${messages.length}, valid messages: ${validMessages.length}`);
    recentMessages.forEach((msg, index) => {
      console.log(`Recent message ${index}: isMe=${msg.isMe}, date=${msg.date}, body='${(msg.body || '').substring(0, 50)}...'`);
    });

    // Find the most recent message from "Them" (the person we're replying to) that has actual content
    const lastMessageFromThem = [...recentMessages].reverse().find(
      (msg) => !msg.isMe && (msg.body || '').trim().length > 0 && (msg.body || '').trim() !== '...'
    );

    if (!lastMessageFromThem) {
      // If all messages are from "Me", we can't generate a reply
      console.warn("No messages from 'Them' found in conversation - all messages are from 'Me'");
      // Check if there are any messages at all
      const allMessagesFromMe = validMessages.every((msg) => msg.isMe);
      if (allMessagesFromMe) {
        return "I cannot generate reply suggestions because all messages in this conversation are from me. I need a message from the other person to generate replies.";
      }
      return 'Generate 3-5 short, friendly reply suggestions for a text message conversation.';
    }

    // Build conversation context showing recent messages (all should have content now)
    const conversationLines: string[] = [];
    recentMessages.forEach((msg) => {
      const sender = msg.isMe ? 'Me' : 'Them';
      const text = (msg.body || '').trim();
      conversationLines.push(`${sender}: ${text}`);
    });

    const conversationText = conversationLines.join('\n');
    const messageToReplyTo = (lastMessageFromThem.body || '').trim();

    console.log(`Selected message to reply to (date=${lastMessageFromThem.date}): '${messageToReplyTo}'`);
    console.log(`Conversation context:\n${conversationText}`);

    // Count how many messages from "Them" are in the context
    const themMessagesCount = recentMessages.filter((msg) => !msg.isMe).length;
    const focusInstruction =
      themMessagesCount > 1
        ? 'Generate 3-5 reply suggestions for the MOST RECENT message from "Them" (the last message from "Them" shown above).'
        : 'Generate 3-5 reply suggestions for the message from "Them" shown above.';

    return `Here is the recent conversation context:

${conversationText}

${focusInstruction} Use the conversation context to understand what's being discussed, but focus your replies on responding to that message from "Them".

CRITICAL: The conversation context is complete above. Generate 3-5 numbered reply suggestions (1-5) immediately. Do NOT ask questions, request more information, or refuse to generate replies. Just output the numbered suggestions.

Reply suggestions:`;
  }

  /**
   * Parse AI response into list of reply suggestions
   */
  private parseReplySuggestions(response: string): string[] {
    // First, try to find the section that contains the actual suggestions
    // Look for patterns like "Reply suggestions:" or numbered lists
    const lowerResponse = response.toLowerCase();

    // Find the start of suggestions section (after "Reply suggestions:" or similar)
    const suggestionStartMarkers = ['reply suggestions:', 'suggestions:', 'here are', '1.', '1)'];

    let startIndex = 0;
    for (const marker of suggestionStartMarkers) {
      const markerIndex = lowerResponse.indexOf(marker);
      if (markerIndex >= 0) {
        // Start from after the marker, or from the numbered item if marker is "1."
        startIndex = marker === '1.' || marker === '1)' ? markerIndex : markerIndex + marker.length;
        break;
      }
    }

    // Extract the relevant portion (from start to end, or first 1000 chars)
    const relevantText = startIndex > 0 ? response.substring(startIndex).substring(0, 1000) : response.substring(0, 1000);

    console.log(`Extracted relevant text for parsing: ${relevantText.substring(0, 200)}`);

    // Split by lines and filter for numbered suggestions
    const suggestions: string[] = [];
    const lines = relevantText.split('\n');
    const processedLines = new Set<number>(); // Track which lines we've already processed

    lines.forEach((line, index) => {
      // Skip if we've already processed this line (e.g., as a reply to a previous label)
      if (processedLines.has(index)) {
        return;
      }
      const trimmed = line.trim();

      // Skip empty lines
      if (trimmed.length === 0) return;

      // Skip lines that look like explanations or prompts
      const lowerTrimmed = trimmed.toLowerCase();
      if (
        lowerTrimmed.startsWith('here are') ||
        lowerTrimmed.startsWith('based on') ||
        lowerTrimmed.startsWith('conversation:') ||
        lowerTrimmed.startsWith('you are') ||
        lowerTrimmed.startsWith('important:') ||
        lowerTrimmed.startsWith('return only') ||
        lowerTrimmed.startsWith('do not include') ||
        lowerTrimmed.includes('thinking') ||
        lowerTrimmed.includes("let me") ||
        lowerTrimmed.includes("i'll") ||
        lowerTrimmed.includes('i will') ||
        (trimmed.startsWith('(') && trimmed.endsWith(')'))
      ) {
        console.log(`Skipping explanatory line: ${trimmed}`);
        return;
      }

      console.log(`Processing line ${index}: ${trimmed}`);

      // Match patterns like "1. Text" or "1) Text"
      const numberPrefixRegex = /^\d+[.)]\s*(.+)/;
      const match = trimmed.match(numberPrefixRegex);

      if (match) {
        const labelOrSuggestion = match[1].trim();

        // Check if this is a label (ends with colon OR contains markdown bold OR is short without quotes)
        let suggestion: string | null = null;
        const hasMarkdownBold = labelOrSuggestion.includes('**') || labelOrSuggestion.includes('__');
        const isLabel =
          labelOrSuggestion.endsWith(':') ||
          hasMarkdownBold ||
          (labelOrSuggestion.length < 50 && !labelOrSuggestion.includes('"') && !labelOrSuggestion.includes("'"));

        if (isLabel) {
          // This is a label, look for the actual reply on the next non-empty line
          console.log(`Found label '${labelOrSuggestion}', looking for reply on next line`);
          let nextLineIndex = index + 1;
          while (nextLineIndex < lines.length && nextLineIndex < index + 5) {
            // Limit search to next 5 lines
            const nextLine = lines[nextLineIndex].trim();
            if (nextLine.length === 0) {
              nextLineIndex++;
              continue;
            }
            // Skip explanatory lines in parentheses
            if (nextLine.startsWith('(') && nextLine.endsWith(')')) {
              nextLineIndex++;
              continue;
            }
            // Skip lines that are just labels (numbered items)
            if (numberPrefixRegex.test(nextLine)) {
              // This is another numbered item, stop looking
              break;
            }
            // Found the actual reply - it should be in quotes or be actual text
            suggestion = nextLine;
            processedLines.add(nextLineIndex); // Mark this line as processed
            console.log(`Found reply on line ${nextLineIndex}: ${suggestion}`);
            break;
          }
        } else {
          // This is the actual suggestion, not a label
          suggestion = labelOrSuggestion;
        }

        if (!suggestion) {
          console.log(`No reply found for label '${labelOrSuggestion}'`);
          return;
        }

        // Remove quotes
        suggestion = suggestion.replace(/^["']|["']$/g, '').trim();

        console.log(`Extracted suggestion: ${suggestion}`);

        // Skip if it looks like an explanation or prompt fragment
        const lowerSuggestion = suggestion.toLowerCase();
        if (
          lowerSuggestion.startsWith('here are') ||
          lowerSuggestion.startsWith('based on') ||
          lowerSuggestion.startsWith('conversation:') ||
          lowerSuggestion.startsWith('you are') ||
          lowerSuggestion.startsWith('important:') ||
          lowerSuggestion.startsWith('return only') ||
          lowerSuggestion.startsWith('do not include') ||
          lowerSuggestion.includes('thinking') ||
          lowerSuggestion.includes("let me") ||
          lowerSuggestion.includes("i'll") ||
          lowerSuggestion.includes('i will') ||
          lowerSuggestion.includes('reply suggestions') ||
          (lowerSuggestion.includes('[insert') || lowerSuggestion.includes('[topic]')) ||
          (lowerSuggestion.endsWith(':') && !lowerSuggestion.includes('"') && !lowerSuggestion.includes("'")) ||
          (hasMarkdownBold && !suggestion.includes('"') && !suggestion.includes("'"))
        ) {
          console.log(`Skipping suggestion that looks like explanation: ${suggestion}`);
          return;
        }

        // Remove any meta-text like "(casual)", "[friendly]", etc.
        suggestion = suggestion.replace(/\([^)]*\)/g, '').replace(/\[[^\]]*\]/g, '').trim();

        // Clean markdown and LLM prefixes
        const cleanedSuggestion = this.cleanSuggestion(suggestion);

        console.log(`Cleaned suggestion: ${cleanedSuggestion}`);

        if (cleanedSuggestion.length > 0 && cleanedSuggestion.length > 3) {
          suggestions.push(cleanedSuggestion);
        }
      }
    });

    // Fallback: if no numbered suggestions found, split by newlines and take non-empty lines
    if (suggestions.length === 0) {
      console.warn(`No numbered suggestions found, using fallback parsing. Response: ${response.substring(0, 200)}`);
      response
        .split('\n')
        .map((line) => this.cleanSuggestion(line.trim()))
        .filter((line) => line.length > 0 && line.length > 5)
        .slice(0, 5)
        .forEach((suggestion) => suggestions.push(suggestion));
    }

    // Limit to 5 suggestions
    return suggestions.slice(0, 5);
  }

  /**
   * Clean a suggestion by removing markdown formatting and common LLM prefixes
   */
  private cleanSuggestion(text: string): string {
    let cleaned = text;

    // Remove markdown bold/italic markers
    cleaned = cleaned.replace(/\*\*([^*]+)\*\*/g, '$1'); // **text**
    cleaned = cleaned.replace(/__([^_]+)__/g, '$1'); // __text__
    cleaned = cleaned.replace(/\*([^*]+)\*/g, '$1'); // *text*
    cleaned = cleaned.replace(/_([^_]+)_/g, '$1'); // _text_

    // Remove surrounding quotes of all types
    cleaned = cleaned.replace(/^["']|["']$/g, '');
    cleaned = cleaned.replace(/^[""]|[""]$/g, ''); // curly double quotes
    cleaned = cleaned.replace(/^['']|['']$/g, ''); // curly single quotes
    cleaned = cleaned.replace(/^[«]|[»]$/g, ''); // guillemets

    // Remove any remaining stray quote characters at start/end
    cleaned = cleaned.replace(/^["''«»]+|["''«»]+$/g, '');

    // Handle "Label: Actual reply" pattern - extract the part after colon if present
    // This handles cases like "Short encouragement: Keep going!" or "Friendly follow-up: How are you?"
    const colonIndex = cleaned.indexOf(':');
    if (colonIndex > 0 && colonIndex < cleaned.length - 1) {
      const afterColon = cleaned.substring(colonIndex + 1).trim();
      // Only use the part after colon if it looks like actual message content
      if (afterColon.length > 3 && !afterColon.includes(':')) {
        cleaned = afterColon;
      }
    }

    // Final cleanup - remove quotes again
    cleaned = cleaned.replace(/^["']|["']$/g, '');
    cleaned = cleaned.replace(/^["''«»]+|["''«»]+$/g, '');

    return cleaned.trim();
  }
}

