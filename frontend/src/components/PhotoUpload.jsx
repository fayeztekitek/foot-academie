import { useState, useRef } from 'react';
import { Camera, X } from 'lucide-react';

function getInitials(prenom, nom) {
  return ((prenom?.[0] || '') + (nom?.[0] || '')).toUpperCase();
}

export function PhotoUpload({ value, onChange, prenom, nom, size = 80 }) {
  const inputRef = useRef(null);

  const handleFile = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) {
      alert('La photo ne doit pas dépasser 2 Mo');
      return;
    }
    // accept="image/*" is cosmetic and bypassable: enforce raster MIME types.
    // SVG is excluded — it can carry scripts (stored-XSS vector).
    if (!['image/png', 'image/jpeg', 'image/gif', 'image/webp'].includes(file.type)) {
      alert('Format accepté : PNG, JPG, GIF ou WEBP');
      return;
    }
    const reader = new FileReader();
    reader.onload = () => onChange(reader.result);
    reader.readAsDataURL(file);
  };

  return (
    <div className="flex flex-col items-center gap-2">
      <div
        className="relative rounded-full overflow-hidden cursor-pointer group"
        style={{ width: size, height: size, background: '#F0EEE4' }}
        onClick={() => inputRef.current?.click()}
      >
        {value ? (
          <img src={value} alt="Photo" className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full flex items-center justify-center font-bebas text-2xl" style={{ color: 'var(--pitch-dark)' }}>
            {getInitials(prenom, nom)}
          </div>
        )}
        <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
          <Camera size={20} className="text-white" />
        </div>
      </div>
      {value && (
        <button
          type="button"
          onClick={() => onChange(null)}
          className="text-xs flex items-center gap-1 text-red cursor-pointer"
        >
          <X size={12} /> Supprimer
        </button>
      )}
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        onChange={handleFile}
        className="hidden"
      />
    </div>
  );
}

export function PhotoAvatar({ photoUrl, prenom, nom, size = 40 }) {
  if (photoUrl) {
    return (
      <img
        src={photoUrl}
        alt={`${prenom} ${nom}`}
        className="rounded-full object-cover"
        style={{ width: size, height: size }}
      />
    );
  }
  return (
    <div className="p-avatar" style={{ width: size, height: size, fontSize: size * 0.35 }}>
      {getInitials(prenom, nom)}
    </div>
  );
}
