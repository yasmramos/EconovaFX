/** @type {import('@sveltejs/kit').LayoutLoad} */
export async function load() {
  return {};
}

// Enable prerendering for static site generation
export const prerender = true;
