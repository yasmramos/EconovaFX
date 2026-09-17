import adapter from '@sveltejs/adapter-static';

/** @type {import('@sveltejs/kit').Config} */
const config = {
  kit: {
    adapter: adapter({
      pages: '../src/main/resources/web',
      assets: '../src/main/resources/web',
      fallback: 'index.html',
      precompress: false,
      strict: true
    })
  }
};

export default config;
