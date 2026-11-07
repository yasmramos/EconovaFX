<script>
  export let variant = 'default'; // default | secondary | outline | destructive
  export let className = '';

  const variants = {
    default: 'border-transparent bg-primary text-primary-foreground',
    secondary: 'border-transparent bg-secondary text-secondary-foreground',
    outline: 'text-foreground',
    destructive: 'border-transparent bg-destructive text-destructive-foreground'
  };

  $: cls = `inline-flex items-center rounded-md border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none ${variants[variant]} ${className}`;
</script>

<span class={cls}>
  <slot />
</span>
