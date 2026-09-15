export default function BarChart({ data, height = 200 }) {
  if (!data || data.length === 0) return null;

  const maxValue = Math.max(...data.map(d => d.value), 1);

  return (
    <div className="w-full" style={{ height }}>
      <div className="flex items-end gap-1 h-full">
        {data.map((item, index) => (
          <div key={index} className="flex-1 flex flex-col items-center justify-end h-full">
            <div className="text-xs font-medium mb-1" style={{ color: 'var(--ink)' }}>{item.value}</div>
            <div
              className="w-full rounded-t"
              style={{ height: `${(item.value / maxValue) * 80}%`, minHeight: '4px', background: 'var(--grass)' }}
            />
            <div className="text-[10px] mt-1 text-center truncate w-full" style={{ color: 'var(--ink-soft)' }}>{item.label}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
