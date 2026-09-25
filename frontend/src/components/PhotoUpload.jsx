import { useState, useRef } from 'react';
import { Camera, X } from 'lucide-react';

function getInitials(prenom, nom) {
  return ((prenom?.[0] || '') + (nom?.[0] || '')).toUpperCase();
}

const MAX_DIMENSION = 1024;
const JPEG_QUALITY = 0.82;

function loadImage(src) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error('decode'));
    img.src = src;
  });
}

export function PhotoUpload({ value, onChange, prenom, nom, size = 80 }) {
  const inputRef = useRef(null);
  const [compressing, setCompressing] = useState(false);

  const handleFile = async (e) => {
    const file = e.target.files?.[0];
    if (inputRef.current) inputRef.current.value = '';
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
    // Downscale on-device: a <2 Mo camera shot inflates ~33% in base64 and
    // used to exceed the server limit (HTTP 400, silent failure). A 1024px
    // JPEG is ~150-400 Ko and plenty for avatars and detail views.
    setCompressing(true);
    const objectUrl = URL.createObjectURL(file);
    try {
      const img = await loadImage(objectUrl);
      const scale = Math.min(1, MAX_DIMENSION / Math.max(img.naturalWidth, img.naturalHeight));
      const w = Math.max(1, Math.round(img.naturalWidth * scale));
      const h = Math.max(1, Math.round(img.naturalHeight * scale));
      const canvas = document.createElement('canvas');
      canvas.width = w;
      canvas.height = h;
      canvas.getContext('2d').drawImage(img, 0, 0, w, h);
      onChange(canvas.toDataURL('image/jpeg', JPEG_QUALITY));
    } catch {
      // Undecodable image (e.g. HEIC on some browsers): fall back to the
      // original bytes; the server still validates type and size.
      const reader = new FileReader();
      reader.onload = () => onChange(reader.result);
      reader.readAsDataURL(file);
    } finally {
      URL.revokeObjectURL(objectUrl);
      setCompressing(false);
    }
  };

  return (
    <div className="flex flex-col items-center gap-2">
      <div
        className="relative rounded-full overflow-hidden cursor-pointer group"
        style={{ width: size, height: size, background: '#F0EEE4', opacity: compressing ? 0.6 : 1 }}
        onClick={() => { if (!compressing) inputRef.current?.click(); }}
      >
        {value ? (
          <img src={value} alt="Photo" className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full flex items-center justify-center font-bebas text-2xl" style={{ color: 'var(--pitch-dark)' }}>
            {compressing ? '…' : getInitials(prenom, nom)}
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
