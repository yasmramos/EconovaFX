<script>
  import '../app.css';
  import Button from '$lib/components/ui/button.svelte';
  import Card from '$lib/components/ui/card.svelte';
  import CardHeader from '$lib/components/ui/card-header.svelte';
  import CardTitle from '$lib/components/ui/card-title.svelte';
  import CardDescription from '$lib/components/ui/card-description.svelte';
  import CardContent from '$lib/components/ui/card-content.svelte';
  import CardFooter from '$lib/components/ui/card-footer.svelte';
  import Badge from '$lib/components/ui/badge.svelte';

  const sections = [
    {
      title: 'Dashboard',
      description: 'Vista financiera en tiempo real con métricas clave y gráficos.',
      icon:
        'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
      badge: 'En vivo',
      badgeVariant: 'default'
    },
    {
      title: 'Cuentas',
      description: 'Gestiona el catálogo de cuentas y la estructura contable.',
      icon:
        'M3 10h18M7 15h1m4 0h1m-7 4h1m4 0h1m-7 4h18M5 7h14a2 2 0 012 2v10a2 2 0 01-2 2H5a2 2 0 01-2-2V9a2 2 0 012-2z',
      badge: '24 activas',
      badgeVariant: 'secondary'
    },
    {
      title: 'Transacciones',
      description: 'Registra y da seguimiento a asientos y comprobantes.',
      icon:
        'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
      badge: 'Nuevas',
      badgeVariant: 'outline'
    }
  ];
</script>

<div class="min-h-screen bg-background">
  <!-- Header -->
  <header class="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
    <div class="container flex h-14 items-center justify-between px-4">
      <div class="flex items-center gap-2">
        <div class="flex h-7 w-7 items-center justify-center rounded-lg bg-primary text-primary-foreground">
          <svg class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M7 4v14m0 0V6a4 4 0 014-4h1a3 3 0 013 3v1a3 3 0 01-3 3H7m0 10a2 2 0 002 2h2a2 2 0 002-2m-4 0a2 2 0 01-2-2m4 0a2 2 0 002 2h2a2 2 0 002-2" />
          </svg>
        </div>
        <span class="font-semibold tracking-tight">EconoNova FX</span>
      </div>
      <nav class="flex items-center gap-6 text-sm text-muted-foreground">
        <a href="#" class="transition-colors hover:text-foreground">Inicio</a>
        <a href="#" class="transition-colors hover:text-foreground">Cuentas</a>
        <a href="#" class="transition-colors hover:text-foreground">Reportes</a>
      </nav>
      <div class="flex items-center gap-2">
        <Button variant="ghost" size="sm">Iniciar sesión</Button>
        <Button size="sm">Comenzar</Button>
      </div>
    </div>
  </header>

  <!-- Hero -->
  <section class="container py-16 text-center">
    <Badge variant="secondary" class="mb-4">Sistema de contabilidad</Badge>
    <h1 class="text-4xl font-bold tracking-tight sm:text-5xl">EconoNova FX Web UI</h1>
    <p class="mx-auto mt-4 max-w-2xl text-lg text-muted-foreground">
      Sistema de contabilidad moderno construido con SvelteKit, Tailwind CSS y una interfaz al estilo de shadcn/ui.
    </p>
    <div class="mt-8 flex items-center justify-center gap-4">
      <Button size="lg">Explorar módulos</Button>
      <Button variant="outline" size="lg">Ver documentación</Button>
    </div>
  </section>

  <!-- Cards -->
  <main class="container pb-16">
    <div class="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
      {#each sections as section}
        <Card class="transition-shadow hover:shadow-md">
          <CardHeader>
            <div class="mb-2 flex h-10 w-10 items-center justify-center rounded-lg bg-muted">
              <svg class="h-5 w-5 text-foreground" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d={section.icon} />
              </svg>
            </div>
            <div class="flex items-center justify-between">
              <CardTitle>{section.title}</CardTitle>
              <Badge variant={section.badgeVariant}>{section.badge}</Badge>
            </div>
            <CardDescription>{section.description}</CardDescription>
          </CardHeader>
          <CardContent class="pb-2">
            <p class="text-xs text-muted-foreground">Módulo disponible en la aplicación de escritorio.</p>
          </CardContent>
          <CardFooter>
            <Button variant="link" class="px-0">Abrir módulo →</Button>
          </CardFooter>
        </Card>
      {/each}
    </div>
  </main>

  <footer class="border-t py-8 text-center text-sm text-muted-foreground">
    <p>&copy; 2024 EconoNova FX. Todos los derechos reservados.</p>
  </footer>
</div>
