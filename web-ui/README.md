# EconoNova FX Web UI

Modern web interface for EconoNova FX accounting system built with SvelteKit and Tailwind CSS.

## Prerequisites

- Node.js 18+ 
- npm or pnpm

## Installation

```bash
npm install
```

## Development

Run the development server:

```bash
npm run dev
```

The app will be available at `http://localhost:5173`

## Build

Build the static site and copy to Java resources:

```bash
npm run build
```

This will:
1. Build the SvelteKit app with Vite
2. Output static files to `build/`
3. Copy all files to `../src/main/resources/web/` for JavaFX to serve

## Project Structure

```
web-ui/
├── src/
│   ├── routes/          # SvelteKit routes
│   │   ├── +layout.svelte
│   │   └── +page.svelte
│   ├── lib/             # Shared components
│   └── app.css          # Global styles with Tailwind
├── static/              # Static assets
├── svelte.config.js     # SvelteKit configuration
├── vite.config.js       # Vite configuration
├── tailwind.config.js   # Tailwind CSS configuration
└── postcss.config.js    # PostCSS configuration
```

## Features

- **SvelteKit**: Modern reactive framework
- **Tailwind CSS**: Utility-first CSS framework
- **Static Site Generation**: Pre-rendered pages for fast loading
- **SPA Routing**: Client-side navigation with fallback to index.html
- **ES2017 Target**: Compatible with JavaFX 17 WebView

## Integration with JavaFX

The built static files are served by a local HTTP server running inside the Java application at `http://127.0.0.1:<port>/`. This avoids CORS issues with ES modules in JavaFX WebView.
