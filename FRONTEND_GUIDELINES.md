# Frontend Guidelines — Dark Editorial Design System

> Este documento define la estética, componentes y convenciones del frontend.
> Claude Code debe leerlo antes de generar o modificar cualquier archivo de UI.

---

## Stack

| Capa       | Tecnología                                                    |
|------------|---------------------------------------------------------------|
| Markup     | HTML5 semántico                                               |
| Estilos    | CSS puro (`src/main/resources/static/css/main.css`) + estilos inline por página si el componente es exclusivo |
| Scripts    | Vanilla JS ES2020+ (`js/main.js`)                            |
| Gráficas   | Chart.js 4.4.1 (CDN cdnjs)                                   |
| Fuentes    | **Inter** 400/500/600/700 + **Instrument Serif** italic 400 (Google Fonts) |

No usar frameworks CSS externos (Bootstrap, Tailwind, etc.).  
No usar React, Vue ni ningún framework JS en páginas del dashboard.  
El panel de Tweaks (`js/tweaks-panel.jsx`) usa React vía CDN y Babel standalone — solo en la landing.

---

## Estética principal

**Dark editorial SaaS.** Negro puro como base, tipografía grande y confiada, componentes liquid glass, animaciones cinematográficas de entrada, parallax en el hero.

- Background base: `#000` puro
- Foreground: `#fff`
- Texto muted: `rgba(255,255,255,0.45–0.65)`
- Bordes: `rgba(255,255,255,0.08–0.12)`
- Cards: `rgba(255,255,255,0.025–0.04)`

---

## Tokens CSS (`:root`)

```css
:root {
  --accent:      #4ade80;   /* verde fútbol — acción principal, CTA, tags */
  --t-speed:     1;         /* multiplicador de velocidad de transición */
  --t-dur:       calc(0.82s * var(--t-speed));
  --t-fast:      calc(0.34s * var(--t-speed));
  --ease-out:    cubic-bezier(0.22, 1, 0.36, 1);
  --ease-io:     cubic-bezier(0.65, 0, 0.35, 1);
  --orb-opacity: 0.22;
  --veil:        0;         /* overlay extra sobre imagen hero */
}
```

**Usar siempre las variables, nunca hex directos en CSS** salvo en los gradientes decorativos de KPIs (que usan colores fijos por diseño).

### Colores de acento secundarios (no tocar salvo rediseño)

Usados en gradientes de KPIs, barras de progreso y gráficas:

```
#a78bfa  purple
#60a5fa  blue
#f472b6  pink
#34d399  green
#fbbf24  amber
#f87171  red
```

### Regla de color para Chart.js

Canvas no puede leer variables CSS. Usar los valores del objeto `CHART_THEME` en `js/main.js`:

```js
purple:    'rgba(167,139,250,0.75)'
blue:      'rgba(96,165,250,0.60)'
neutral:   'rgba(148,163,184,0.40)'
grid:      'rgba(255,255,255,0.05)'
ticks:     'rgba(226,232,240,0.40)'
tooltipBg: 'rgba(15,12,30,0.90)'
```

---

## Tipografía

| Uso                          | Familia          | Peso | Tamaño |
|------------------------------|------------------|------|--------|
| Hero / display grande        | Inter            | 700  | `clamp(52px, 9.5vw, 96px)`, `letter-spacing: -3px` |
| Section titles               | Inter            | 700  | `clamp(34px, 5.5vw, 56px)`, `letter-spacing: -2px` |
| CTA title                    | Inter            | 700  | `clamp(38px, 7vw, 70px)`, `letter-spacing: -2.5px` |
| Acento editorial (1 palabra) | Instrument Serif | 400 italic | Heredado del titular |
| Stats grandes                | Inter            | 700  | `clamp(44px, 7vw, 68px)`, `letter-spacing: -2.5px` |
| Navbar / nav links           | Inter            | 500  | 13–13.5px |
| Cuerpo / descripción         | Inter            | 400  | 14.5–17px, `line-height: 1.65` |
| Labels uppercase             | Inter            | 700  | 10–11px, `letter-spacing: 1.8px`, color `--accent` |
| Tablas / filas               | Inter            | 400/500 | 12–13px |

`text-wrap: balance` siempre en titulares (`h1`, `h2`).

---

## Liquid Glass

Clase base `.liqglass` para contenedores elevados (navbar scrolled, pills, cards premium):

```css
.liqglass {
  background: rgba(255,255,255,0.03);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  position: relative; overflow: hidden;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.09);
}
.liqglass::before {
  content: '';
  position: absolute; inset: 0; border-radius: inherit; padding: 1px;
  background: linear-gradient(180deg,
    rgba(255,255,255,0.38) 0%, rgba(255,255,255,0.08) 20%,
    rgba(255,255,255,0) 50%,
    rgba(255,255,255,0.08) 80%, rgba(255,255,255,0.32) 100%);
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor; mask-composite: exclude; pointer-events: none;
}
```

---

## Sistema de animaciones

### Entrada (blur + slide + fade)

```css
.ha {
  opacity: 0; transform: translateY(38px); filter: blur(6px);
  transition:
    opacity   var(--t-dur) var(--ease-out),
    transform var(--t-dur) var(--ease-out),
    filter    var(--t-dur) ease;
}
.ha.vis { opacity: 1; transform: none; filter: none; }

/* Stagger */
.d1 { transition-delay: calc(0.09s * var(--t-speed)); }
.d2 { transition-delay: calc(0.18s * var(--t-speed)); }
.d3 { transition-delay: calc(0.28s * var(--t-speed)); }
.d4 { transition-delay: calc(0.38s * var(--t-speed)); }
.d5 { transition-delay: calc(0.48s * var(--t-speed)); }
```

Activar con `IntersectionObserver` (`threshold: 0.08`) que añade `.vis`:

```js
const io = new IntersectionObserver(entries => {
  entries.forEach(e => {
    if (e.isIntersecting) { e.target.classList.add('vis'); io.unobserve(e.target); }
  });
}, { threshold: 0.08 });
document.querySelectorAll('.ha').forEach(el => io.observe(el));
```

Los elementos del hero se activan con `setTimeout` en stagger (no IntersectionObserver).

### Page in

```css
body { animation: pageIn calc(0.55s * var(--t-speed)) var(--ease-out) both; }
@keyframes pageIn { from { opacity: 0; } to { opacity: 1; } }
```

### Hover en cards

```css
.feat-card:hover {
  background: rgba(255,255,255,0.046);
  border-color: rgba(255,255,255,0.16);
  transform: translateY(-5px);
  box-shadow: 0 20px 60px -20px rgba(0,0,0,0.6);
}
```

---

## Orbs de fondo

Siempre presentes. Tres divs dentro de `<div class="orbs">`, `position: fixed`, `z-index: 0`.

```html
<div class="orbs">
  <div class="orb o1"></div>
  <div class="orb o2"></div>
  <div class="orb o3"></div>
</div>
```

```css
.orbs { position: fixed; inset: 0; z-index: 0; pointer-events: none; overflow: hidden; }
.orb  { position: absolute; border-radius: 50%; filter: blur(90px);
        opacity: var(--orb-opacity);
        animation: orb-drift 14s ease-in-out infinite alternate; }
.o1 { width: 600px; height: 600px;
      background: radial-gradient(circle, oklch(0.42 0.13 142), transparent 70%);
      top: 30%; left: -200px; animation-duration: 17s; }
.o2 { width: 480px; height: 480px;
      background: radial-gradient(circle, #1d4ed8, transparent 70%);
      bottom: 10%; right: -160px; animation-duration: 11s; animation-delay: -5s; }
.o3 { width: 360px; height: 360px;
      background: radial-gradient(circle, #4a044e, transparent 70%);
      bottom: 38%; left: 22%; animation-duration: 20s; animation-delay: -9s; }
@keyframes orb-drift {
  0%   { transform: translate(0, 0) scale(1); }
  100% { transform: translate(30px, 20px) scale(1.08); }
}
```

El contenido siempre con `position: relative; z-index: 1`.

---

## Componentes

### Navbar

`position: fixed`. Transparente por defecto, `scrolled` al superar 50px:

```css
.navbar.scrolled {
  background: rgba(0,0,0,0.72);
  backdrop-filter: blur(20px);
  border-bottom: 0.5px solid rgba(255,255,255,0.1);
}
```

Botones navbar: `.btn-ghost` (texto semitransparente) y `.btn-solid` (blanco, texto negro, `border-radius: 8px`).

### Cards de contenido

```css
background: rgba(255,255,255,0.025);
border: 0.5px solid rgba(255,255,255,0.08);
border-radius: 18px;
```

### KPI strip

4 cards en grid. Valores con gradiente de texto via `background-clip: text`:

```html
<div class="pkpi">
  <div class="pkpi-val" style="background: linear-gradient(90deg,#fbbf24,#f87171);
       -webkit-background-clip:text; -webkit-text-fill-color:transparent; background-clip:text;">
    14
  </div>
  <div class="pkpi-lbl">Jugadores</div>
</div>
```

Gradientes por posición (mantener orden):
- K1 → amber→rojo `#fbbf24, #f87171`
- K2 → purple→pink `#a78bfa, #f472b6`
- K3 → blue→green `#60a5fa, #34d399`
- K4 → pink→purple `#f472b6, #a78bfa`

### Tabla

Clase `tbl`. Cabeceras con `cursor: pointer` para sorting. Separadores de zona con clase `sep`.

Badges de ranking:
```html
<span class="rnk rnk-primary">1</span>   <!-- top -->
<span class="rnk rnk-secondary">5</span> <!-- medio-alto -->
<span class="rnk rnk-neutral">10</span>
<span class="rnk rnk-danger">18</span>   <!-- descenso/error -->
```

### Status badges

```html
<span class="status-badge status-success">Activo</span>
<span class="status-badge status-warning">Pendiente</span>
<span class="status-badge status-danger">Error</span>
<span class="status-badge status-info">Sync</span>
```

### Barra de progreso

```html
<div class="bar-wrap">
  <div class="bar-fill" style="width: 75%"></div>
</div>
```

Variantes: `bar-fill` (purple→blue), `bar-fill-success`, `bar-fill-warning`, `bar-fill-danger`.

### Botones

```html
<a href="..." class="btn-cta">Acción principal →</a>       <!-- blanco, pill, negro -->
<a href="..." class="btn-outline">Secundario</a>            <!-- borde semitransparente -->
<button class="btn-solid">Acción en formulario</button>     <!-- blanco, rect, negro -->
<button class="btn-ghost">Entrar →</button>                 <!-- sin fondo, texto muted -->
```

### Section tag

```html
<span class="sec-tag">Funciones</span>
```
```css
.sec-tag { font-size: 11px; font-weight: 700; text-transform: uppercase;
           letter-spacing: 1.8px; color: var(--accent); }
```

### Word reveal (testimonial / quote)

Cada palabra es un `<span class="w">`. En scroll, calcular progreso y añadir `.lit`:

```css
.w     { color: rgba(255,255,255,0.18); transition: color 0.28s ease; }
.w.lit { color: #fff; }
```

---

## JS utilities (main.js)

| Función | Descripción |
|---------|-------------|
| `createBarChart(canvasId, labels, data, colorFn)` | Crea gráfica de barras |
| `createLineChart(canvasId, labels, datasets)` | Crea gráfica de líneas |
| `renderTable(tbodyId, rows, columns)` | Renderiza filas con `{key, label, class, render}` |
| `buildTableHeader(theadRowId, columns, onSort)` | Headers con sorting |
| `updateKpi(index, {label, value, sub})` | Actualiza una card KPI por índice (0–3) |
| `apiFetch(path, options)` | GET/POST a `/api` + path, devuelve JSON o null |

---

## Responsive

| Breakpoint | Cambios |
|------------|---------|
| ≤ 720px    | Navbar sin nav-links; feat-grid y stats-row a 1 columna; preview-kpis a 2 columnas; footer en columna |
| ≤ 640px    | KPI strip a 2 columnas; tablas con scroll horizontal |

---

## Archivos estáticos

```
src/main/resources/static/
├── landing.html          ← Landing page pública
├── login.html
├── index.html            ← Dashboard principal
├── css/main.css          ← Tokens, reset, componentes base
├── js/main.js            ← Utilidades (apiFetch, renderTable, charts, CHART_THEME)
├── js/tweaks-panel.jsx   ← Panel de tweaks React (solo landing)
├── assets/               ← SVGs de logo y favicon
└── uploads/              ← Imágenes subidas (fotos jugadores, hero)
```

---

## Reglas generales para Claude Code

1. **Usar siempre las variables CSS** — nunca hex directos salvo en gradientes decorativos fijos.
2. **Fondo de orbs es fijo** — primer elemento del `<body>`, `position: fixed`, `z-index: 0`. No tocar.
3. **Toda animación de entrada usa `.ha` + `.vis`** — activar con IntersectionObserver o setTimeout en hero.
4. **Chart.js siempre con `CHART_THEME`** del `main.js`.
5. **No mezclar estilos inline** con tokens del tema, salvo `style="width:X%"` para barras dinámicas.
6. **No añadir librerías nuevas** sin solicitud explícita. Chart.js es la única dependencia JS del dashboard.
7. **Tipografía editorial** (`Instrument Serif italic`) solo para UNA palabra clave en titulares `<em>`. No abusar.
8. **`text-wrap: balance`** siempre en `h1`, `h2`, `h3` de secciones.
9. **Responsive** — respetar los breakpoints de la tabla de arriba. No romper el grid en mobile.
10. **Sin comentarios** en HTML/CSS excepto separadores de sección (`/* ── NAVBAR ── */`).
