# bbloom.vdom

Yet another Virtual DOM library (in ClojureScript).

## Goals

- Low-level design with minimal policy (like [virtual-dom][0]).
- Enable idiomatic use from ClojureScript.
- Be as fast as necessary for responsive user interfaces.
- Provide a richer abstraction of the DOM. See "novelty" below.
- Support modern browsers.

## Non-Goals

Or just goals to be realized by a higher layer.

- Normalize browser behavior.
- Provide a widget or component model (see [React.js][1]).
- Win any benchmarks.
- Be usable from JavaScript.

## Novelty

What makes this Virtual DOM library different?

- Represent the DOM as a graph.
- Manage detatched nodes.
- Support re-parenting (eg for drag-and-drop).

More details on these novelties coming soon.

## Usage

Don't. At least not yet.

## Contributing

Ping me if you're interested, I'm excited to discuss what you've got in mind.

## License

Copyright © 2015 Brandon Bloom

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.


[0]: https://github.com/Matt-Esch/virtual-dom
[1]: https://facebook.github.io/react/
