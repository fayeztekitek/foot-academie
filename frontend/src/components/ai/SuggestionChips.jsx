import { Sparkles } from 'lucide-react';

export default function SuggestionChips({ suggestions, onSelect }) {
  return (
    <div className="flex flex-wrap gap-2">
      {suggestions.map((suggestion, i) => (
        <button
          key={i}
          onClick={() => onSelect(suggestion)}
          className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-full hover:bg-emerald-100 hover:border-emerald-300 transition-colors"
        >
          <Sparkles size={12} />
          {suggestion}
        </button>
      ))}
    </div>
  );
}
