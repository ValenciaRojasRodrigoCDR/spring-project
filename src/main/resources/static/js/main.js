/**
 * main.js — Glassmorphism Dashboard Utilities
 * Conectar con la API de Spring Boot y renderizar los componentes.
 */

/* ============================================================
   CONFIG — adapta BASE_URL a tu contexto Spring Boot
   ============================================================ */
const BASE_URL = '/api';

/* ============================================================
   CHART.JS THEME — colores hardcoded (canvas no lee CSS vars)
   ============================================================ */
const CHART_THEME = {
  purple:      'rgba(167, 139, 250, 0.75)',
  blue:        'rgba( 96, 165, 250, 0.60)',
  neutral:     'rgba(148, 163, 184, 0.40)',
  purpleLine:  '#a78bfa',
  blueLine:    '#60a5fa',
  grid:        'rgba(255,255,255,0.05)',
  ticks:       'rgba(226,232,240,0.40)',
  tooltipBg:   'rgba(15,12,30,0.90)',
  tooltipBrd:  'rgba(255,255,255,0.10)',
  tooltipTxt:  '#e2e8f0',
  tooltipVal:  '#a78bfa',
};

/* ============================================================
   CHART — crea una barra con el tema glassmorphism
   ============================================================ */
function createBarChart(canvasId, labels, data, colorFn) {
  const ctx = document.getElementById(canvasId);
  if (!ctx) return null;

  return new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        data,
        backgroundColor: colorFn ? data.map((_, i) => colorFn(i)) : CHART_THEME.purple,
        borderRadius: 6,
        borderSkipped: false,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: CHART_THEME.tooltipBg,
          borderColor: CHART_THEME.tooltipBrd,
          borderWidth: 0.5,
          titleColor: CHART_THEME.tooltipTxt,
          bodyColor:  CHART_THEME.tooltipVal,
        }
      },
      scales: {
        x: {
          ticks: { color: CHART_THEME.ticks, font: { size: 10, family: 'Outfit' }, maxRotation: 35, minRotation: 35, autoSkip: false },
          grid: { display: false },
          border: { display: false }
        },
        y: {
          ticks: { color: CHART_THEME.ticks, font: { size: 10, family: 'Outfit' } },
          grid: { color: CHART_THEME.grid },
          border: { display: false }
        }
      }
    }
  });
}

/* ============================================================
   CHART — línea
   ============================================================ */
function createLineChart(canvasId, labels, datasets) {
  const ctx = document.getElementById(canvasId);
  if (!ctx) return null;

  const colors = [CHART_THEME.purpleLine, CHART_THEME.blueLine];
  const ds = datasets.map((d, i) => ({
    label: d.label,
    data: d.data,
    borderColor: colors[i % colors.length],
    backgroundColor: colors[i % colors.length].replace(')', ', 0.1)').replace('rgb', 'rgba'),
    borderWidth: 2,
    tension: 0.4,
    pointRadius: 3,
    pointHoverRadius: 5,
  }));

  return new Chart(ctx, {
    type: 'line',
    data: { labels, datasets: ds },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: datasets.length > 1, labels: { color: CHART_THEME.ticks, font: { size: 11, family: 'Outfit' } } },
        tooltip: {
          backgroundColor: CHART_THEME.tooltipBg,
          borderColor: CHART_THEME.tooltipBrd,
          borderWidth: 0.5,
          titleColor: CHART_THEME.tooltipTxt,
          bodyColor: CHART_THEME.tooltipVal,
        }
      },
      scales: {
        x: {
          ticks: { color: CHART_THEME.ticks, font: { size: 10, family: 'Outfit' } },
          grid: { color: CHART_THEME.grid },
          border: { display: false }
        },
        y: {
          ticks: { color: CHART_THEME.ticks, font: { size: 10, family: 'Outfit' } },
          grid: { color: CHART_THEME.grid },
          border: { display: false }
        }
      }
    }
  });
}

/* ============================================================
   TABLE — renderiza filas con clases del tema
   ============================================================ */
/**
 * @param {string} tbodyId  - id del <tbody>
 * @param {Array}  rows     - array de objetos con los datos
 * @param {Array}  columns  - [{key, label, class, render}]
 */
function renderTable(tbodyId, rows, columns) {
  const tbody = document.getElementById(tbodyId);
  if (!tbody) return;
  tbody.innerHTML = '';
  rows.forEach(row => {
    const tr = document.createElement('tr');
    columns.forEach(col => {
      const td = document.createElement('td');
      if (col.class) td.className = col.class;
      td.innerHTML = col.render ? col.render(row[col.key], row) : (row[col.key] ?? '—');
      tr.appendChild(td);
    });
    tbody.appendChild(tr);
  });
}

/* ============================================================
   TABLE HEADER — genera headers con sorting
   ============================================================ */
function buildTableHeader(theadRowId, columns, onSort) {
  const tr = document.getElementById(theadRowId);
  if (!tr) return;
  let sortCol = null, sortAsc = true;
  tr.innerHTML = '';
  columns.forEach((col, i) => {
    const th = document.createElement('th');
    if (col.right) th.classList.add('tr');
    th.innerHTML = `${col.label} <span class="sa" id="sort-icon-${i}"></span>`;
    th.onclick = () => {
      if (sortCol === i) sortAsc = !sortAsc; else { sortCol = i; sortAsc = true; }
      columns.forEach((_, j) => {
        const ic = document.getElementById(`sort-icon-${j}`);
        if (ic) ic.textContent = j === i ? (sortAsc ? '▲' : '▼') : '';
      });
      if (onSort) onSort(col.key, sortAsc);
    };
    tr.appendChild(th);
  });
}

/* ============================================================
   KPI — actualiza una card
   ============================================================ */
function updateKpi(index, { label, value, sub }) {
  const cards = document.querySelectorAll('.kpi');
  const card  = cards[index];
  if (!card) return;
  if (label) card.querySelector('.kpi-lbl').textContent = label;
  if (value !== undefined) card.querySelector('.kpi-val').textContent = value;
  if (sub)   card.querySelector('.kpi-sub').textContent = sub;
}

/* ============================================================
   API HELPERS
   ============================================================ */
async function apiUpload(path, file) {
  const token = localStorage.getItem('jwt_token');
  const form  = new FormData();
  form.append('file', file);
  try {
    const res = await fetch(`${BASE_URL}${path}`, {
      method: 'POST',
      headers: token ? { 'Authorization': `Bearer ${token}` } : {},
      body: form,
    });
    if (res.status === 401) { window.location.replace('/login.html'); return null; }
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    console.error('[Upload Error]', path, err);
    return null;
  }
}


async function apiFetch(path, options = {}) {
  const token = localStorage.getItem('jwt_token');
  try {
    const res = await fetch(`${BASE_URL}${path}`, {
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      },
      ...options,
    });
    if (res.status === 401) { window.location.replace('/login.html'); return null; }
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    console.error('[API Error]', path, err);
    return null;
  }
}

/* ============================================================
   INIT — punto de entrada
   Sustituye este bloque por tu lógica de negocio
   ============================================================ */
document.addEventListener('DOMContentLoaded', () => {
  console.log('[Dashboard] Glassmorphism UI ready');

  const username = localStorage.getItem('username');
  if (username) {
    const el = document.getElementById('app-title');
    if (el) el.textContent = username;
  }

  const input    = document.getElementById('excel-input');
  const filename = document.getElementById('excel-filename');
  const btn      = document.getElementById('excel-upload-btn');
  const status   = document.getElementById('excel-status');

  if (input) {
    input.addEventListener('change', () => {
      filename.textContent = input.files[0]?.name ?? 'Sin fichero seleccionado';
      status.textContent = '';
    });
  }

  if (btn) {
    btn.addEventListener('click', async () => {
      const file = input?.files[0];
      if (!file) { status.style.color = 'var(--am)'; status.textContent = 'Selecciona un fichero primero.'; return; }
      status.style.color = 'var(--txt2)'; status.textContent = 'Subiendo…';
      try {
        const form = new FormData();
        form.append('file', file);
        const res = await fetch(`${BASE_URL}/excel/upload`, { method: 'POST', body: form });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        status.style.color = 'var(--gr)'; status.textContent = '✓ Fichero procesado correctamente.';
        console.log('[Excel] data:', data);
      } catch (err) {
        status.style.color = 'var(--re)'; status.textContent = `✗ Error: ${err.message}`;
      }
    });
  }
});
