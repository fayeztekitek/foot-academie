import { useState, useRef, useEffect } from 'react';
import { MessageSquare, X, Send, Trash2, Upload, Settings, Bot, User } from 'lucide-react';
import { aiApi } from '../../api/ai';
import MessageBubble from './MessageBubble';
import SuggestionChips from './SuggestionChips';
import DocumentUpload from './DocumentUpload';

export default function ChatWidget({ currentPage }) {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  const [showUpload, setShowUpload] = useState(false);
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    if (isOpen && suggestions.length === 0) {
      loadSuggestions();
    }
  }, [isOpen, currentPage]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const loadSuggestions = async () => {
    try {
      const data = await aiApi.getSuggestions(currentPage);
      setSuggestions(data);
    } catch {
      setSuggestions([
        "Résumé du tableau de bord",
        "Joueurs en retard de paiement",
        "Planning de la semaine",
      ]);
    }
  };

  const sendMessage = async (text) => {
    if (!text.trim() || isLoading) return;

    const userMsg = { role: 'user', content: text };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setIsLoading(true);

    try {
      const response = await aiApi.chat(text, currentPage);
      const aiMsg = { role: 'assistant', content: response.message, success: response.success };
      setMessages(prev => [...prev, aiMsg]);
    } catch (err) {
      setMessages(prev => [...prev, {
        role: 'assistant',
        content: 'Désolé, une erreur est survenue. Veuillez réessayer.',
        success: false,
      }]);
    } finally {
      setIsLoading(false);
      loadSuggestions();
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage(input);
    }
  };

  const clearChat = async () => {
    setMessages([]);
    await aiApi.clearHistory();
  };

  const handleDocumentUpload = async (file) => {
    setShowUpload(false);
    setIsLoading(true);
    setMessages(prev => [...prev, {
      role: 'user',
      content: `📄 Document: ${file.name}`,
    }]);

    try {
      const response = await aiApi.analyzeDocument(file, currentPage);
      setMessages(prev => [...prev, {
        role: 'assistant',
        content: response.message,
        success: response.success,
        metadata: response.metadata,
      }]);
    } catch {
      setMessages(prev => [...prev, {
        role: 'assistant',
        content: "Erreur lors de l'analyse du document.",
        success: false,
      }]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      {/* Floating Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="fixed bottom-24 right-4 md:bottom-6 md:right-6 z-50 w-14 h-14 rounded-full bg-gradient-to-br from-emerald-600 to-emerald-700 text-white shadow-lg hover:shadow-xl transition-all duration-200 flex items-center justify-center hover:scale-105 active:scale-95"
        style={{ minHeight: '44px', minWidth: '44px' }}
        aria-label="Assistant IA Nadi"
      >
        {isOpen ? <X size={24} /> : <MessageSquare size={24} />}
      </button>

      {/* Chat Panel */}
      {isOpen && (
        <div className="fixed bottom-40 right-4 md:bottom-20 md:right-6 z-50 w-[calc(100vw-2rem)] max-w-md bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col overflow-hidden"
             style={{ maxHeight: 'calc(100vh - 12rem)' }}>
          {/* Header */}
          <div className="bg-gradient-to-r from-emerald-600 to-emerald-700 text-white px-4 py-3 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Bot size={20} />
              <div>
                <h3 className="font-semibold text-sm">Nadi AI</h3>
                <p className="text-xs text-emerald-100">Assistant intelligent</p>
              </div>
            </div>
            <div className="flex items-center gap-1">
              <button onClick={clearChat} className="p-2 rounded-lg hover:bg-emerald-600 transition-colors"
                      title="Effacer l'historique">
                <Trash2 size={16} />
              </button>
              <button onClick={() => setShowUpload(!showUpload)} className="p-2 rounded-lg hover:bg-emerald-600 transition-colors"
                      title="Analyser un document">
                <Upload size={16} />
              </button>
            </div>
          </div>

          {/* Document Upload */}
          {showUpload && (
            <DocumentUpload onUpload={handleDocumentUpload} onClose={() => setShowUpload(false)} />
          )}

          {/* Messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3 min-h-0">
            {messages.length === 0 && (
              <div className="text-center text-gray-400 text-sm py-8">
                <Bot size={40} className="mx-auto mb-3 text-emerald-300" />
                <p className="font-medium text-gray-500">Bonjour ! Je suis Nadi AI</p>
                <p className="mt-1">Posez-moi une question sur votre académie</p>
              </div>
            )}
            {messages.map((msg, i) => (
              <MessageBubble key={i} message={msg} />
            ))}
            {isLoading && (
              <div className="flex items-center gap-2 text-gray-400 text-sm">
                <div className="flex gap-1">
                  <span className="w-2 h-2 bg-emerald-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                  <span className="w-2 h-2 bg-emerald-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                  <span className="w-2 h-2 bg-emerald-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
                </div>
                <span>Nadi réfléchit...</span>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Suggestions */}
          {suggestions.length > 0 && messages.length < 3 && (
            <div className="px-4 pb-2">
              <SuggestionChips suggestions={suggestions} onSelect={sendMessage} />
            </div>
          )}

          {/* Input */}
          <div className="border-t border-gray-200 p-3">
            <div className="flex items-end gap-2">
              <textarea
                ref={inputRef}
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Posez votre question..."
                className="flex-1 resize-none rounded-xl border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 max-h-20"
                rows={1}
                disabled={isLoading}
              />
              <button
                onClick={() => sendMessage(input)}
                disabled={!input.trim() || isLoading}
                className="p-2 rounded-xl bg-emerald-600 text-white hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                style={{ minHeight: '40px', minWidth: '40px' }}
              >
                <Send size={18} />
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
