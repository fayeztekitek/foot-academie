import { Bot, User, AlertCircle } from 'lucide-react';
import ReactMarkdown from 'react-markdown';

export default function MessageBubble({ message }) {
  const isUser = message.role === 'user';

  return (
    <div className={`flex gap-2 ${isUser ? 'justify-end' : 'justify-start'}`}>
      {!isUser && (
        <div className="w-7 h-7 rounded-full bg-emerald-100 flex items-center justify-center flex-shrink-0 mt-1">
          <Bot size={16} className="text-emerald-600" />
        </div>
      )}

      <div className={`max-w-[80%] rounded-2xl px-3 py-2 text-sm ${
        isUser
          ? 'bg-emerald-600 text-white rounded-br-md'
          : message.success === false
            ? 'bg-red-50 text-red-800 border border-red-200 rounded-bl-md'
            : 'bg-gray-100 text-gray-800 rounded-bl-md'
      }`}>
        {isUser ? (
          <p className="whitespace-pre-wrap">{message.content}</p>
        ) : (
          <div className="prose prose-sm max-w-none prose-p:my-1 prose-ul:my-1 prose-li:my-0">
            <ReactMarkdown>{message.content}</ReactMarkdown>
          </div>
        )}

        {message.metadata && !isUser && (
          <div className="mt-2 pt-2 border-t border-gray-200 text-xs text-gray-500">
            {Object.entries(message.metadata).map(([key, value]) => (
              <span key={key} className="inline-block mr-3 mb-1">
                <span className="font-medium">{key}:</span> {String(value)}
              </span>
            ))}
          </div>
        )}

        {message.success === false && !isUser && (
          <div className="flex items-center gap-1 mt-1 text-xs text-red-500">
            <AlertCircle size={12} />
            <span>Erreur</span>
          </div>
        )}
      </div>

      {isUser && (
        <div className="w-7 h-7 rounded-full bg-emerald-600 flex items-center justify-center flex-shrink-0 mt-1">
          <User size={16} className="text-white" />
        </div>
      )}
    </div>
  );
}
