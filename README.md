# bbloom.vdom

Yet another Virtual DOM library (in ClojureScript).

## Usage

Don't. At least not yet.

But if you want to play with it, checkout [playground.cljs][2].

## Goals

- Low-level design with minimal policy (like [virtual-dom][0]).
- Enable idiomatic use from ClojureScript.
- Be as fast as necessary for responsive user interfaces.
- Provide a richer abstraction of the DOM. See "novelty" below.

## Non-Goals

Or just goals to be realized by a higher layer.

- Assign node IDs automatically.
- Normalize browser behavior.
- Provide a widget or component model (a la [React.js][1]).
- Win any benchmarks.
- Be usable from JavaScript.
- Support older browsers.

## Novelty

What makes this Virtual DOM library different?

- Represent the DOM as an immutable graph value.
- Manage detached nodes.
- Support re-parenting (eg for drag-and-drop).

## Motivation

There's a great many problems that more-feature-complete DOM libraries such
as React.js tackle. A low-level virtual DOM library is not an appropriate
platform for application developers. However, a high-level virtual DOM library
bakes in a large amount of policy that may be inappropriate for alternative
frameworks. For example, React uses runtime techniques to normalize browser
behavior, but given compiler support (such as ClojureScript macros), you may
choose a more static approach to work around browser quirks or optimize
inline styles.

I wanted a low-level DOM library for experimenting with high-level frameworks
from ClojureScript. My intention is to enforce strict system layering, but
ownership over every layer enables me to make changes in the appropriate
place.

## Approach

Generalized tree diffing has high algorithmic complexity, so virtual DOM
implementations take advantage of the time dimension by diffing trees
level-by-level on the assumption that there are typically few large structural
changes. Worst case becomes linear and early-out tests for subtrees means that
typical diffing is logarithmic.

An alternative approach to capitalize on the same assumptions is to do the
equivalent of a "hash join" when calculating a diff. If every node has a
unique ID, and those IDs are stable over time, fast linear diffing is easily
achieved by visiting each node in the new virtual DOM and looking it up in the
previous virtual DOM's hash table.

This approach allows more freedom of movement for nodes around the tree, at the
cost of the additional bookkeeping required to ensure ID stability. A component
model built on this foundation can provide automatic-ID assignment via
structural path + discriminator key, which matches the level-by-level approach.
While the virtual DOM diffing remains linear, the higher-level can easily
recover the typically-logarithmic times, which is where the constant factors
are higher (for data fetching, change detection, etc). However, such a
component model could also represent IDs beyond the scope of a single parent,
and therefore support reparenting of real DOM nodes when virtual nodes are
reparented. This reparenting is useful for complex interactions such as
drag-and-drop between containers, pop-out panels, or components shared between
different views.

To this end, the virtual DOM is not represented as a tree, but as a graph.
The graph is itself represented as several indexes over structured nodes
which have an ID, type/tag, properties, a parent, and ordered children. The
graph is designed to be "mounted" on to zero or more places in a "host" DOM.
Virtual nodes can reparent between mounts and even exist detached from any
mount. This more closely models the actual DOM APIs, where nodes can move
freely and may have a null parent. Immutability is used pervasively and
reference equality is maintained wherever possible for fast equality checks.
Patches are represented in terms of traditional DOM API manipulations, so that
there's a nearly one-to-one operational interpretation.

## Contributing

Ping me if you're interested, I'm excited to discuss what you've got in mind.

## License

Copyright © 2015 Brandon Bloom

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.


[0]: https://github.com/Matt-Esch/virtual-dom
[1]: https://facebook.github.io/react/
[2]: ./src/bbloom/vdom/playground.cljs
