# Frontend Guidelines — Glassmorphism Design System

> Este documento define la estética, componentes y convenciones del frontend.
> Claude Code debe leerlo antes de generar o modificar cualquier archivo de UI.

---

## Stack

| Capa       | Tecnología                          |
|------------|-------------------------------------|
| Markup     | HTML5 semántico                     |
| Estilos    | CSS puro (`src/main/resources/static/css/main.css`) |
| Scripts    | Vanilla JS ES2020+ (`js/main.js`)   |
| Gráficas   | Chart.js 4.4.1 (CDN cdnjs)         |
| Fuente     | **Outfit** (Google Fonts) — pesos 300/400/500/600/700/900 |

No usar frameworks CSS externos (Bootstrap, Tailwind, etc.).  
No usar React, Vue ni ningún framework JS.

---

## Paleta de colores — variables CSS

Definidas en `:root` de `main.css`. **Usar siempre variables, nunca hex directos en CSS.**

```css
/* Fondos */
--g1: #0d0b1e   /* fondo base — muy oscuro */
--g2: #1a1040
--g3: #0f1f3d

/* Acentos */
--pu: #a78bfa   /* purple  — acción principal, CTA */
--bl: #60a5fa   /* blue    — información, secundario */
--pk: #f472b6   /* pink    — acento decorativo */
--gr: #34d399   /* green   — éxito, estado activo */
--am: #fbbf24   /* amber   — advertencia */
--re: #f87171   /* red     — error, peligro */

/* Superficies glass */
--glass:     rgba(255,255,255,0.06)
--glass-brd: rgba(255,255,255,0.12)
--glass-hov: rgba(255,255,255,0.10)

/* Texto */
--txt:  #e2e8f0          /* primario */
--txt2: rgba(226,232,240,0.60)   /* secundario / subtítulos */
--txt3: rgba(226,232,240,0.35)   /* terciario / labels */
```

### Regla de color para Chart.js

Canvas no puede leer variables CSS. Usar siempre los valores del objeto `CHART_THEME` en `js/main.js`:

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

| Uso                  | Peso | Tamaño |
|----------------------|------|--------|
| Título app           | 700  | 18px   |
| Valor KPI grande     | 700  | 26px   |
| Encabezado panel     | 600  | 11px uppercase + letter-spacing 1px |
| Cuerpo / tabla       | 400  | 12–13px |
| Labels / badges      | 500  | 10–11px |
| Subtítulo / muted    | 400  | 10–11px, color `--txt2` |

Nunca usar font-weight 800 ni 900 en UI (solo display grande si hubiera hero).

---

## Componentes

### Glass card

Clase base para cualquier contenedor elevado:

```html
<div class="glass panel">
  <div class="ptitle">Título sección</div>
  <!-- contenido -->
</div>
```

Propiedades clave:
```css
background: rgba(255,255,255,0.06);
border: 0.5px solid rgba(255,255,255,0.12);
border-radius: 16px;
backdrop-filter: blur(16px);
```

En hover: `background` sube a `0.10`, borde a `0.18`.

### KPI strip

4 cards en grid. Cada KPI tiene `kpi-lbl`, `kpi-val`, `kpi-sub`.

Gradientes de valor por posición (no cambiar el orden):
- `.k1` → amber→rojo
- `.k2` → purple→pink
- `.k3` → blue→green
- `.k4` → color sólido rojo (`--re`) para métricas de alerta

```html
<div class="kpi-strip">
  <div class="kpi glass k1"> ... </div>
  <div class="kpi glass k2"> ... </div>
  <div class="kpi glass k3"> ... </div>
  <div class="kpi glass k4"> ... </div>
</div>
```

### Tabla

Usar siempre `class="tbl"`. Cabeceras con `cursor: pointer` para sorting.  
Separadores de zona: añadir clase `sep` al `<tr>` que abre una nueva zona.

Badges de ranking:
```html
<span class="rnk rnk-primary">1</span>   <!-- purple — top -->
<span class="rnk rnk-secondary">5</span> <!-- blue — medio-alto -->
<span class="rnk rnk-neutral">10</span>  <!-- sin fondo -->
<span class="rnk rnk-danger">18</span>   <!-- red — descenso/error -->
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

Variantes de color: `bar-fill` (purple→blue), `bar-fill-success`, `bar-fill-warning`, `bar-fill-danger`.

### Botones

```html
<button class="btn">Secundario</button>
<button class="btn btn-primary">Acción principal</button>
```

### Live indicator (header)

```html
<div class="live-dot"></div>
<span class="live-lbl">En curso</span>
<span class="jornada">v1.0</span>
```

---

## Fondo animado (orbs)

Siempre presente. Tres divs con `class="orb orb1/2/3"` dentro de `<div class="bg">`.  
**No modificar** los valores de blur, opacidad ni animación `drift`.  
Los orbs son `position: fixed` y `z-index: 0`; el contenido usa `z-index: 1`.

```html
<div class="bg">
  <div class="orb orb1"></div>
  <div class="orb orb2"></div>
  <div class="orb orb3"></div>
</div>
```

---

## JS utilities (main.js)

Funciones disponibles sin importar nada:

| Función | Descripción |
|---------|-------------|
| `createBarChart(canvasId, labels, data, colorFn)` | Crea gráfica de barras |
| `createLineChart(canvasId, labels, datasets)` | Crea gráfica de líneas |
| `renderTable(tbodyId, rows, columns)` | Renderiza filas con `{key, label, class, render}` |
| `buildTableHeader(theadRowId, columns, onSort)` | Headers con sorting |
| `updateKpi(index, {label, value, sub})` | Actualiza una card KPI por índice (0–3) |
| `apiFetch(path, options)` | GET/POST a `BASE_URL + path`, devuelve JSON o null |

### Conexión con Spring Boot

Cambiar `BASE_URL` al inicio de `main.js` si el contexto path cambia:

```js
const BASE_URL = '/api';  // ajustar si hay context-path en application.yml
```

Llamar endpoints así:

```js
const data = await apiFetch('/v1/standings');
if (data) renderTable('table-body', data, columns);
```

---

## Arquitectura hexagonal — estructura de paquetes

```
src/main/java/com/project/
│
├── domain/                        ← Núcleo del negocio (sin dependencias externas)
│   ├── model/                     ← Entidades y value objects
│   └── exception/                 ← Excepciones de dominio
│
├── application/                   ← Casos de uso
│   ├── port/
│   │   ├── in/                    ← Interfaces que el exterior llama (UseCase)
│   │   └── out/                   ← Interfaces que el dominio necesita (Repository, etc.)
│   └── service/                   ← Implementación de los casos de uso
│
└── infrastructure/                ← Adaptadores (detalle técnico)
    ├── adapter/
    │   ├── in/
    │   │   └── web/               ← Controllers REST (@RestController)
    │   └── out/
    │       └── persistence/       ← Repositorios JPA / implementaciones de port/out
    └── config/                    ← @Configuration de Spring
```

### Convenciones de naming

| Capa            | Sufijo esperado              |
|-----------------|------------------------------|
| Port entrada    | `UseCase`, `Query`           |
| Port salida     | `Port`, `Repository`         |
| Servicios       | `Service`                    |
| Controllers     | `Controller`                 |
| DTOs entrada    | `Request`, `Command`         |
| DTOs salida     | `Response`, `Dto`            |
| Mappers         | `Mapper`                     |

---

## Reglas generales para Claude Code

1. **No modificar `main.css` sin motivo** — toda variación de color debe usar las variables.
2. **Chart.js siempre con el tema** — usar `CHART_THEME` del `main.js`.
3. **Nunca mezclar estilos inline** con los colores del tema excepto en `style="width:X%"` para barras dinámicas.
4. **Clases de tabla son semánticas** — `rnk-primary` = top/éxito, `rnk-danger` = error/descenso.
5. **El fondo de orbs es fijo** — siempre el primer elemento del body, nunca tocarlo.
6. **Responsive** — el grid pasa a 1 columna en ≤640px, las KPIs a 2 columnas. No romper esto.
7. **Sin librerías nuevas** — Chart.js es la única dependencia JS. Cualquier adición debe ser explícitamente solicitada.
8. **Archivos estáticos en** `src/main/resources/static/` — CSS en `css/`, JS en `js/`, imágenes/iconos en `assets/`.
