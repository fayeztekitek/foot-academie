import { useRef } from 'react';
import { Upload, FileText, Image, Table2, X } from 'lucide-react';

export default function DocumentUpload({ onUpload, onClose }) {
  const fileInputRef = useRef(null);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      onUpload(file);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file) {
      onUpload(file);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const acceptedTypes = [
    { ext: '.pdf', icon: FileText, label: 'PDF', color: 'text-red-500' },
    { ext: '.csv', icon: Table2, label: 'CSV', color: 'text-green-500' },
    { ext: '.png,.jpg,.jpeg', icon: Image, label: 'Images', color: 'text-blue-500' },
    { ext: '.txt,.md', icon: FileText, label: 'Texte', color: 'text-gray-500' },
  ];

  return (
    <div className="border-b border-gray-200 bg-gray-50 p-4">
      <div className="flex items-center justify-between mb-3">
        <h4 className="text-sm font-medium text-gray-700">Analyser un document</h4>
        <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
          <X size={16} />
        </button>
      </div>

      <div
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        className="border-2 border-dashed border-gray-300 rounded-xl p-4 text-center hover:border-emerald-400 transition-colors cursor-pointer"
        onClick={() => fileInputRef.current?.click()}
      >
        <Upload size={24} className="mx-auto text-gray-400 mb-2" />
        <p className="text-sm text-gray-600">
          Glissez un fichier ici ou cliquez pour sélectionner
        </p>
        <div className="flex justify-center gap-3 mt-3">
          {acceptedTypes.map(({ ext, icon: Icon, label, color }) => (
            <div key={ext} className="flex items-center gap-1 text-xs text-gray-500">
              <Icon size={14} className={color} />
              <span>{label}</span>
            </div>
          ))}
        </div>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept=".pdf,.csv,.png,.jpg,.jpeg,.txt,.md"
        onChange={handleFileChange}
        className="hidden"
      />

      <p className="text-xs text-gray-400 mt-2 text-center">
        PDF, CSV, Images (OCR), Texte — Max 10MB
      </p>
    </div>
  );
}
