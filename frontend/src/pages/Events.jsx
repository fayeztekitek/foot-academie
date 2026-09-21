import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { eventsApi, playersApi, categoriesApi } from '../api';
import { useAuth } from '../hooks/useAuth';
import {
  Plus,
  X,
  Trash2,
  Calendar,
  MapPin,
  Users,
  Check,
  ChevronLeft,
  ChevronRight,
  Search,
} from 'lucide-react';

const TYPE_LABELS = {
  TOURNOI: 'Tournoi',
  MATCH: 'Match',
  ENTRAINEMENT_SPECIAL: 'Entraînement spécial',
  STAGE: 'Stage',
  AUTRE: 'Autre',
};

const TYPE_COLORS = {
  TOURNOI: { bg: '#e8f5e9', color: '#2e7d32', border: '#a5d6a7' },
  MATCH: { bg: '#fff3e0', color: '#e65100', border: '#ffcc80' },
  ENTRAINEMENT_SPECIAL: { bg: '#e3f2fd', color: '#1565c0', border: '#90caf9' },
  STAGE: { bg: '#fce4ec', color: '#c62828', border: '#ef9a9a' },
  AUTRE: { bg: '#f3e5f5', color: '#6a1b9a', border: '#ce93d8' },
};

const STATUT_LABELS = {
  INVITE: 'Invité',
  CONFIRME: 'Confirmé',
  REFUSE: 'Refusé',
};

const STATUT_COLORS = {
  INVITE: { bg: '#fff8e1', color: '#f57f17', border: '#ffe082' },
  CONFIRME: { bg: '#e8f5e9', color: '#2e7d32', border: '#a5d6a7' },
  REFUSE: { bg: '#fbe9e7', color: '#c62828', border: '#ef9a9a' },
};

const MONTHS_FR = [
  'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
  'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre',
];

const DAYS_FR = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

function getMonthDays(year, month) {
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const daysInMonth = lastDay.getDate();
  let startDay = firstDay.getDay() - 1;
  if (startDay < 0) startDay = 6;
  const days = [];
  for (let i = 0; i < startDay; i++) {
    days.push(null);
  }
  for (let d = 1; d <= daysInMonth; d++) {
    days.push(d);
  }
  return days;
}

function formatDate(dateStr) {
  const d = new Date(dateStr);
  return d.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short', year: 'numeric' });
}

function formatTime(timeStr) {
  return timeStr ? timeStr.slice(0, 5) : '';
}

function formatDateISO(dateStr) {
  if (!dateStr) return '';
  return dateStr.split('T')[0];
}

function getEventsForDay(events, year, month, day) {
  if (!events) return [];
  return events.filter((ev) => {
    const d = new Date(ev.dateDebut);
    return d.getFullYear() === year && d.getMonth() === month && d.getDate() === day;
  });
}

export default function Events() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const isAdmin = user?.role === 'ADMIN' || user?.role === 'COACH';

  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth());
  const [selectedDay, setSelectedDay] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);

  const [newEvent, setNewEvent] = useState({
    titre: '',
    type: 'MATCH',
    dateDebut: '',
    dateFin: '',
    heureDebut: '',
    heureFin: '',
    lieu: '',
    terrain: '',
    description: '',
    joueurIds: [],
    categorieIds: [],
  });
  const [eventSearch, setEventSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('');

  const { data: events = [], isLoading: eventsLoading } = useQuery({
    queryKey: ['events', currentYear, currentMonth],
    queryFn: () => eventsApi.getByMonth(currentYear, currentMonth + 1).then(r => r.data || []),
  });

  const { data: players = [] } = useQuery({
    queryKey: ['players'],
    queryFn: () => playersApi.getAll().then(r => r.data?.content || r.data || []),
  });

  const { data: categories = [] } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoriesApi.getAll().then(r => r.data?.content || r.data || []),
  });

  const createMutation = useMutation({
    mutationFn: (data) => eventsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] });
      setShowCreateModal(false);
      resetForm();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => eventsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] });
      setShowDetailModal(false);
      setSelectedEvent(null);
    },
  });

  const convocateMutation = useMutation({
    mutationFn: ({ eventId, data }) => eventsApi.convocate(eventId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] });
    },
  });

  const respondMutation = useMutation({
    mutationFn: ({ convocationId, statut }) => eventsApi.respondConvocation(convocationId, statut),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] });
    },
  });

  function resetForm() {
    setNewEvent({
      titre: '',
      type: 'MATCH',
      dateDebut: '',
      dateFin: '',
      heureDebut: '',
      heureFin: '',
      lieu: '',
      terrain: '',
      description: '',
      joueurIds: [],
      categorieIds: [],
    });
  }

  function prevMonth() {
    if (currentMonth === 0) {
      setCurrentMonth(11);
      setCurrentYear((y) => y - 1);
    } else {
      setCurrentMonth((m) => m - 1);
    }
    setSelectedDay(null);
  }

  function nextMonth() {
    if (currentMonth === 11) {
      setCurrentMonth(0);
      setCurrentYear((y) => y + 1);
    } else {
      setCurrentMonth((m) => m + 1);
    }
    setSelectedDay(null);
  }

  function handleDayClick(day) {
    setSelectedDay(day);
  }

  function handleEventClick(event) {
    setSelectedEvent(event);
    setShowDetailModal(true);
  }

  function togglePlayer(playerId) {
    setNewEvent((prev) => {
      const ids = prev.joueurIds.includes(playerId)
        ? prev.joueurIds.filter((id) => id !== playerId)
        : [...prev.joueurIds, playerId];
      return { ...prev, joueurIds: ids };
    });
  }

  function toggleAllPlayers() {
    if (newEvent.joueurIds.length === players.length) {
      setNewEvent((prev) => ({ ...prev, joueurIds: [] }));
    } else {
      setNewEvent((prev) => ({ ...prev, joueurIds: players.map((p) => p.id) }));
    }
  }

  function toggleCategory(catId) {
    setNewEvent((prev) => {
      const ids = prev.categorieIds.includes(catId)
        ? prev.categorieIds.filter((id) => id !== catId)
        : [...prev.categorieIds, catId];
      return { ...prev, categorieIds: ids };
    });
  }

  function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      titre: newEvent.titre,
      type: newEvent.type,
      dateDebut: newEvent.dateDebut
        ? `${newEvent.dateDebut}T${newEvent.heureDebut || '00:00'}:00`
        : null,
      dateFin: newEvent.dateFin
        ? `${newEvent.dateFin}T${newEvent.heureFin || '23:59'}:00`
        : null,
      lieu: newEvent.lieu,
      terrain: newEvent.terrain,
      description: newEvent.description,
      joueurIds: newEvent.joueurIds,
      categorieIds: newEvent.categorieIds,
    };
    createMutation.mutate(payload);
  }

  function handleDelete() {
    if (selectedEvent && window.confirm('Supprimer cet événement ?')) {
      deleteMutation.mutate(selectedEvent.id);
    }
  }

  function handleRespondConvocation(convocationId, statut) {
    respondMutation.mutate({ convocationId, statut });
  }

  const days = getMonthDays(currentYear, currentMonth);
  const today = new Date();

  const dayEvents = selectedDay
    ? getEventsForDay(events, currentYear, currentMonth, selectedDay)
    : [];

  if (!isAdmin) {
    const myConvocations = [];
    if (events) {
      events.forEach((ev) => {
        if (ev.convocations) {
          ev.convocations.forEach((c) => {
            if (c.joueurId === user?.joueurId || c.utilisateurId === user?.id) {
              myConvocations.push({ ...c, evenement: ev });
            }
          });
        }
      });
    }

    return (
      <div style={{ padding: '2rem', maxWidth: 900, margin: '0 auto' }}>
        <h1
          className="font-bebas"
          style={{ fontSize: '2rem', color: 'var(--pitch-dark)', marginBottom: '1.5rem' }}
        >
          Mes convocations
        </h1>
        {myConvocations.length === 0 ? (
          <div className="panel" style={{ padding: '2rem', textAlign: 'center', color: 'var(--ink-soft)' }}>
            Aucune convocation pour le moment.
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {myConvocations.map((conv) => (
              <div
                key={conv.id}
                className="panel"
                style={{ padding: '1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}
              >
                <div style={{ flex: 1, minWidth: 200 }}>
                  <div style={{ fontWeight: 600, color: 'var(--pitch-dark)', marginBottom: 4 }}>
                    {conv.evenement?.titre}
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--ink-soft)', display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                      <Calendar size={14} />
                      {formatDate(conv.evenement?.dateDebut)}
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                      <MapPin size={14} />
                      {conv.evenement?.lieu}
                    </span>
                  </div>
                  <div style={{ marginTop: 6 }}>
                    <span
                      className="pill"
                      style={{
                        backgroundColor: TYPE_COLORS[conv.evenement?.type]?.bg,
                        color: TYPE_COLORS[conv.evenement?.type]?.color,
                        border: `1px solid ${TYPE_COLORS[conv.evenement?.type]?.border}`,
                        fontSize: '0.75rem',
                      }}
                    >
                      {TYPE_LABELS[conv.evenement?.type] || conv.evenement?.type}
                    </span>
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span
                    className="pill"
                    style={{
                      backgroundColor: STATUT_COLORS[conv.statut]?.bg,
                      color: STATUT_COLORS[conv.statut]?.color,
                      border: `1px solid ${STATUT_COLORS[conv.statut]?.border}`,
                      fontSize: '0.75rem',
                      marginRight: 8,
                    }}
                  >
                    {STATUT_LABELS[conv.statut] || conv.statut}
                  </span>
                  {conv.statut === 'INVITE' && (
                    <>
                      <button
                        className="btn-primary"
                        style={{ padding: '0.4rem 0.75rem', fontSize: '0.8rem' }}
                        onClick={() => handleRespondConvocation(conv.id, 'CONFIRME')}
                        disabled={respondMutation.isPending}
                      >
                        <Check size={14} /> Accepter
                      </button>
                      <button
                        className="btn-ghost"
                        style={{ padding: '0.4rem 0.75rem', fontSize: '0.8rem', color: 'var(--red)' }}
                        onClick={() => handleRespondConvocation(conv.id, 'REFUSE')}
                        disabled={respondMutation.isPending}
                      >
                        <X size={14} /> Refuser
                      </button>
                    </>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem', maxWidth: 1100, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <h1
          className="font-bebas"
          style={{ fontSize: '2rem', color: 'var(--pitch-dark)' }}
        >
          Événements & Tournois
        </h1>
        <button
          className="btn-primary"
          onClick={() => { resetForm(); setShowCreateModal(true); }}
          style={{ display: 'flex', alignItems: 'center', gap: 6 }}
        >
          <Plus size={18} /> Nouvel événement
        </button>
      </div>

      {/* Filters */}
      <div className="flex gap-3 mb-5 flex-wrap items-center">
        <div className="relative flex-1 min-w-[200px] max-w-[340px]">
          <Search size={15} className="absolute left-3 top-2.5" style={{ color: 'var(--ink-soft)' }} />
          <input
            value={eventSearch}
            onChange={e => setEventSearch(e.target.value)}
            placeholder="Rechercher un événement…"
            className="input-field pl-9 text-xs"
          />
        </div>
        <select
          value={typeFilter}
          onChange={e => setTypeFilter(e.target.value)}
          className="input-field text-xs py-1.5 px-3"
        >
          <option value="">Tous les types</option>
          {Object.entries(TYPE_LABELS).map(([key, label]) => (
            <option key={key} value={key}>{label}</option>
          ))}
        </select>
        {(eventSearch || typeFilter) && (
          <button
            onClick={() => { setEventSearch(''); setTypeFilter(''); }}
            className="text-xs flex items-center gap-1 px-2 py-1 rounded border cursor-pointer hover:bg-gray-50"
            style={{ color: 'var(--ink-soft)', borderColor: 'var(--line)' }}
          >
            <X size={12} /> Effacer filtres
          </button>
        )}
      </div>

      <div className="panel" style={{ padding: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <button className="btn-ghost" onClick={prevMonth} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <ChevronLeft size={20} />
          </button>
          <h2
            className="font-bebas"
            style={{ fontSize: '1.4rem', color: 'var(--pitch-dark)', margin: 0 }}
          >
            {MONTHS_FR[currentMonth]} {currentYear}
          </h2>
          <button className="btn-ghost" onClick={nextMonth} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <ChevronRight size={20} />
          </button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 2 }}>
          {DAYS_FR.map((d) => (
            <div
              key={d}
              style={{
                textAlign: 'center',
                fontWeight: 600,
                fontSize: '0.8rem',
                color: 'var(--ink-soft)',
                padding: '0.5rem 0',
                borderBottom: '2px solid var(--line)',
              }}
            >
              {d}
            </div>
          ))}
          {days.map((day, idx) => {
            if (day === null) return <div key={`empty-${idx}`} />;
            const dayEvts = getEventsForDay(events, currentYear, currentMonth, day).filter(ev => {
              if (eventSearch) {
                const q = eventSearch.toLowerCase();
                if (!(ev.titre || '').toLowerCase().includes(q) && !(ev.lieu || '').toLowerCase().includes(q)) return false;
              }
              if (typeFilter && ev.type !== typeFilter) return false;
              return true;
            });
            const isToday =
              today.getFullYear() === currentYear &&
              today.getMonth() === currentMonth &&
              today.getDate() === day;
            const isSelected = selectedDay === day;
            return (
              <div
                key={day}
                onClick={() => handleDayClick(day)}
                style={{
                  minHeight: 70,
                  padding: 4,
                  borderRadius: 6,
                  cursor: 'pointer',
                  border: isSelected
                    ? '2px solid var(--grass)'
                    : isToday
                      ? '2px solid var(--gold)'
                      : '1px solid transparent',
                  backgroundColor: isSelected
                    ? 'var(--grass)'
                    : 'transparent',
                  transition: 'all 0.15s ease',
                }}
              >
                <div
                  style={{
                    fontWeight: isToday ? 700 : 400,
                    fontSize: '0.85rem',
                    color: isSelected ? '#fff' : isToday ? 'var(--gold)' : 'var(--pitch-dark)',
                    marginBottom: 2,
                    textAlign: 'right',
                    paddingRight: 4,
                  }}
                >
                  {day}
                </div>
                {dayEvts.slice(0, 3).map((ev) => (
                  <div
                    key={ev.id}
                    onClick={(e) => { e.stopPropagation(); handleEventClick(ev); }}
                    style={{
                      fontSize: '0.65rem',
                      padding: '1px 4px',
                      borderRadius: 3,
                      marginBottom: 2,
                      backgroundColor: TYPE_COLORS[ev.type]?.bg || '#eee',
                      color: TYPE_COLORS[ev.type]?.color || '#333',
                      border: `1px solid ${TYPE_COLORS[ev.type]?.border || '#ccc'}`,
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                      cursor: 'pointer',
                    }}
                    title={ev.titre}
                  >
                    {ev.titre}
                  </div>
                ))}
                {dayEvts.length > 3 && (
                  <div
                    style={{
                      fontSize: '0.6rem',
                      color: isSelected ? '#fff' : 'var(--ink-soft)',
                      textAlign: 'center',
                    }}
                  >
                    +{dayEvts.length - 3}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {selectedDay !== null && (
        <div className="panel" style={{ padding: '1.5rem', marginTop: '1rem' }}>
          <h3
            className="font-bebas"
            style={{ fontSize: '1.2rem', color: 'var(--pitch-dark)', marginBottom: '1rem' }}
          >
            {selectedDay} {MONTHS_FR[currentMonth]} {currentYear}
          </h3>
          {dayEvents.length === 0 ? (
            <p style={{ color: 'var(--ink-soft)' }}>Aucun événement ce jour.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {dayEvents.map((ev) => (
                <div
                  key={ev.id}
                  onClick={() => handleEventClick(ev)}
                  style={{
                    padding: '1rem',
                    borderRadius: 8,
                    border: `1px solid ${TYPE_COLORS[ev.type]?.border || '#ccc'}`,
                    backgroundColor: TYPE_COLORS[ev.type]?.bg || '#f9f9f9',
                    cursor: 'pointer',
                    transition: 'box-shadow 0.15s ease',
                  }}
                  onMouseEnter={(e) => (e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)')}
                  onMouseLeave={(e) => (e.currentTarget.style.boxShadow = 'none')}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                      <div style={{ fontWeight: 600, color: TYPE_COLORS[ev.type]?.color || '#333', marginBottom: 4 }}>
                        {ev.titre}
                      </div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--ink-soft)', display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                          <Calendar size={13} />
                          {formatTime(ev.heureDebut)} - {formatTime(ev.heureFin)}
                        </span>
                        {ev.lieu && (
                          <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                            <MapPin size={13} /> {ev.lieu}
                          </span>
                        )}
                        {ev.convocations && (
                          <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                            <Users size={13} /> {ev.convocations.length}
                          </span>
                        )}
                      </div>
                    </div>
                    <span
                      className="pill"
                      style={{
                        backgroundColor: TYPE_COLORS[ev.type]?.bg,
                        color: TYPE_COLORS[ev.type]?.color,
                        border: `1px solid ${TYPE_COLORS[ev.type]?.border}`,
                        fontSize: '0.7rem',
                        whiteSpace: 'nowrap',
                      }}
                    >
                      {TYPE_LABELS[ev.type] || ev.type}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {showCreateModal && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0,0,0,0.5)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 1000,
            padding: '1rem',
          }}
          onClick={() => setShowCreateModal(false)}
        >
          <div
            className="panel"
            style={{ width: '100%', maxWidth: 640, maxHeight: '90vh', overflow: 'auto', padding: '2rem' }}
            onClick={(e) => e.stopPropagation()}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2
                className="font-bebas"
                style={{ fontSize: '1.5rem', color: 'var(--pitch-dark)', margin: 0 }}
              >
                Nouvel événement
              </h2>
              <button className="btn-ghost" onClick={() => setShowCreateModal(false)}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label className="label">Titre</label>
                  <input
                    className="input-field"
                    type="text"
                    value={newEvent.titre}
                    onChange={(e) => setNewEvent({ ...newEvent, titre: e.target.value })}
                    required
                    placeholder="Ex: Tournoi U14"
                  />
                </div>

                <div>
                  <label className="label">Type</label>
                  <select
                    className="input-field"
                    value={newEvent.type}
                    onChange={(e) => setNewEvent({ ...newEvent, type: e.target.value })}
                  >
                    {Object.entries(TYPE_LABELS).map(([key, label]) => (
                      <option key={key} value={key}>{label}</option>
                    ))}
                  </select>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div>
                    <label className="label">Date début</label>
                    <input
                      className="input-field"
                      type="date"
                      value={newEvent.dateDebut}
                      onChange={(e) => setNewEvent({ ...newEvent, dateDebut: e.target.value })}
                      required
                    />
                  </div>
                  <div>
                    <label className="label">Date fin</label>
                    <input
                      className="input-field"
                      type="date"
                      value={newEvent.dateFin}
                      onChange={(e) => setNewEvent({ ...newEvent, dateFin: e.target.value })}
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div>
                    <label className="label">Heure début</label>
                    <input
                      className="input-field"
                      type="time"
                      value={newEvent.heureDebut}
                      onChange={(e) => setNewEvent({ ...newEvent, heureDebut: e.target.value })}
                    />
                  </div>
                  <div>
                    <label className="label">Heure fin</label>
                    <input
                      className="input-field"
                      type="time"
                      value={newEvent.heureFin}
                      onChange={(e) => setNewEvent({ ...newEvent, heureFin: e.target.value })}
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div>
                    <label className="label">Lieu</label>
                    <input
                      className="input-field"
                      type="text"
                      value={newEvent.lieu}
                      onChange={(e) => setNewEvent({ ...newEvent, lieu: e.target.value })}
                      placeholder="Stade, terrain..."
                    />
                  </div>
                  <div>
                    <label className="label">Terrain</label>
                    <input
                      className="input-field"
                      type="text"
                      value={newEvent.terrain}
                      onChange={(e) => setNewEvent({ ...newEvent, terrain: e.target.value })}
                      placeholder="Terrain 1, pelouse..."
                    />
                  </div>
                </div>

                <div>
                  <label className="label">Description</label>
                  <textarea
                    className="input-field"
                    rows={3}
                    value={newEvent.description}
                    onChange={(e) => setNewEvent({ ...newEvent, description: e.target.value })}
                    placeholder="Détails de l'événement..."
                    style={{ resize: 'vertical' }}
                  />
                </div>

                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                    <label className="label" style={{ margin: 0 }}>Joueurs</label>
                    <button
                      type="button"
                      className="btn-ghost"
                      onClick={toggleAllPlayers}
                      style={{ fontSize: '0.8rem', padding: '0.2rem 0.5rem' }}
                    >
                      {newEvent.joueurIds.length === players.length ? 'Tout désélectionner' : 'Tout sélectionner'}
                    </button>
                  </div>
                  <div
                    style={{
                      maxHeight: 160,
                      overflow: 'auto',
                      border: '1px solid var(--line)',
                      borderRadius: 6,
                      padding: '0.5rem',
                    }}
                  >
                    {players.length === 0 && (
                      <p style={{ fontSize: '0.85rem', color: 'var(--ink-soft)', margin: 0 }}>Aucun joueur disponible.</p>
                    )}
                    {players.map((p) => (
                      <label
                        key={p.id}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: 8,
                          padding: '0.3rem 0.4rem',
                          borderRadius: 4,
                          cursor: 'pointer',
                          fontSize: '0.85rem',
                          color: 'var(--pitch-dark)',
                        }}
                      >
                        <input
                          type="checkbox"
                          checked={newEvent.joueurIds.includes(p.id)}
                          onChange={() => togglePlayer(p.id)}
                          style={{ accentColor: 'var(--grass)' }}
                        />
                        {p.prenom} {p.nom}
                      </label>
                    ))}
                  </div>
                </div>

                <div>
                  <label className="label">Catégories</label>
                  <div
                    style={{
                      maxHeight: 120,
                      overflow: 'auto',
                      border: '1px solid var(--line)',
                      borderRadius: 6,
                      padding: '0.5rem',
                    }}
                  >
                    {categories.length === 0 && (
                      <p style={{ fontSize: '0.85rem', color: 'var(--ink-soft)', margin: 0 }}>Aucune catégorie disponible.</p>
                    )}
                    {categories.map((cat) => (
                      <label
                        key={cat.id}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: 8,
                          padding: '0.3rem 0.4rem',
                          borderRadius: 4,
                          cursor: 'pointer',
                          fontSize: '0.85rem',
                          color: 'var(--pitch-dark)',
                        }}
                      >
                        <input
                          type="checkbox"
                          checked={newEvent.categorieIds.includes(cat.id)}
                          onChange={() => toggleCategory(cat.id)}
                          style={{ accentColor: 'var(--grass)' }}
                        />
                        {cat.nom}
                      </label>
                    ))}
                  </div>
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
                  <button
                    type="button"
                    className="btn-ghost"
                    onClick={() => setShowCreateModal(false)}
                  >
                    Annuler
                  </button>
                  <button
                    type="submit"
                    className="btn-primary"
                    disabled={createMutation.isPending}
                  >
                    {createMutation.isPending ? 'Création...' : 'Créer'}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}

      {showDetailModal && selectedEvent && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0,0,0,0.5)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 1000,
            padding: '1rem',
          }}
          onClick={() => setShowDetailModal(false)}
        >
          <div
            className="panel"
            style={{ width: '100%', maxWidth: 560, maxHeight: '90vh', overflow: 'auto', padding: '2rem' }}
            onClick={(e) => e.stopPropagation()}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
              <div>
                <h2
                  className="font-bebas"
                  style={{ fontSize: '1.5rem', color: 'var(--pitch-dark)', margin: '0 0 4px' }}
                >
                  {selectedEvent.titre}
                </h2>
                <span
                  className="pill"
                  style={{
                    backgroundColor: TYPE_COLORS[selectedEvent.type]?.bg,
                    color: TYPE_COLORS[selectedEvent.type]?.color,
                    border: `1px solid ${TYPE_COLORS[selectedEvent.type]?.border}`,
                    fontSize: '0.75rem',
                  }}
                >
                  {TYPE_LABELS[selectedEvent.type] || selectedEvent.type}
                </span>
              </div>
              <div style={{ display: 'flex', gap: 6 }}>
                <button
                  className="btn-ghost"
                  style={{ color: 'var(--red)' }}
                  onClick={handleDelete}
                  disabled={deleteMutation.isPending}
                  title="Supprimer"
                >
                  <Trash2 size={18} />
                </button>
                <button className="btn-ghost" onClick={() => setShowDetailModal(false)}>
                  <X size={20} />
                </button>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.9rem', color: 'var(--ink-soft)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <Calendar size={16} />
                <span>{formatDate(selectedEvent.dateDebut)}</span>
                {selectedEvent.heureDebut && (
                  <span> {formatTime(selectedEvent.heureDebut)} - {formatTime(selectedEvent.heureFin)}</span>
                )}
              </div>
              {selectedEvent.lieu && (
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <MapPin size={16} />
                  <span>{selectedEvent.lieu}</span>
                  {selectedEvent.terrain && <span style={{ color: 'var(--ink-soft)' }}>— {selectedEvent.terrain}</span>}
                </div>
              )}
              {selectedEvent.description && (
                <div style={{ marginTop: 4, padding: '0.75rem', backgroundColor: 'var(--line)', borderRadius: 6 }}>
                  {selectedEvent.description}
                </div>
              )}
            </div>

            {selectedEvent.convocations && selectedEvent.convocations.length > 0 && (
              <div style={{ marginTop: '1.25rem' }}>
                <h3
                  className="font-bebas"
                  style={{ fontSize: '1.1rem', color: 'var(--pitch-dark)', marginBottom: '0.75rem' }}
                >
                  Convocations ({selectedEvent.convocations.length})
                </h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {selectedEvent.convocations.map((c) => (
                    <div
                      key={c.id}
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '0.6rem 0.75rem',
                        border: '1px solid var(--line)',
                        borderRadius: 6,
                        fontSize: '0.85rem',
                      }}
                    >
                      <span style={{ color: 'var(--pitch-dark)', fontWeight: 500 }}>
                        {c.joueur?.prenom} {c.joueur?.nom || c.utilisateur?.prenom + ' ' + c.utilisateur?.nom}
                      </span>
                      <span
                        className="pill"
                        style={{
                          backgroundColor: STATUT_COLORS[c.statut]?.bg,
                          color: STATUT_COLORS[c.statut]?.color,
                          border: `1px solid ${STATUT_COLORS[c.statut]?.border}`,
                          fontSize: '0.7rem',
                        }}
                      >
                        {STATUT_LABELS[c.statut] || c.statut}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}