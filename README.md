<div align="center">

# ☕ Macchiato

### Write React in Java. Generate TSX. Bundle from Spring Boot.

[![JDK](https://img.shields.io/badge/JDK-17+-007396?style=for-the-badge&logo=openjdk)](https://openjdk.org)
[![React](https://img.shields.io/badge/React-18+-61DAFB?style=for-the-badge&logo=react)](https://react.dev)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](./LICENSE)

</div>

`macc` is a Java DSL for React: declare pages and components with
annotations and fluent builders, then run **`macc install`** to emit a
typed React/Vite frontend that bundles into Spring Boot's static
resources.

Pair with **[Xpresso](https://github.com/Nandobez/Xpresso)** for the
backend (Rails-style scaffolder) and **[jdp](https://github.com/Nandobez/jdp)**
for dependency hygiene. Or use **[Ristretto](https://github.com/Nandobez/Ristretto)**
as the umbrella CLI for all three.

## Install

```bash
curl -fsSL https://raw.githubusercontent.com/Nandobez/Macchiato/main/install.sh | bash
```

Prerequisites: **JDK 17+**, **mvn**, **git**, **Node 18+**.

## 90-second tour

```bash
xpresso new tasks-app --group io.demo                      # 1) backend
cd tasks-app
xpresso g resource Task title:string done:bool             # 2) JPA CRUD
                                                           # 3) write a @Page in Java
macc new src/main/frontend                                 # 4) scaffold Vite + Tailwind
macc add button card dialog                                # 5) pull shadcn components
macc install                                               # 6) Java → TSX + vite build
mvn spring-boot:run                                        # 7) one server, frontend included
```

Or with watch + dev parallel:

```bash
macc watch &       # re-codegen on every .java change
macc serve         # backend :8080 + vite :5173 with /api proxy
```

## Commands

```
PROJECT
  new <dir>             scaffold Vite + React + Tailwind into <dir>
  install               full build (mvn compile + codegen + npm install + vite build)
  dev                   vite hot reload (frontend only)
  serve                 backend (xpresso s) + vite dev together · /api proxy
  watch                 file watcher · re-runs codegen on every .java change

GENERATE
  g page <Name>         @Page class with @Fetch / @State / @Action
  g component <Name>    custom Java component (reusable across pages)
  g template <Name>     alias of `g component` — name it however you think of it
  g model <Name>        @Model record (shared with the backend, mirrored as TS interface)
  codegen               scan @Page/@Model on the classpath → emit .tsx + types + routes

UI LIBRARY
  add <comp>…           import shadcn-style components + generate Java wrappers
                        ex: macc add button card dialog audio-player

INTEGRATIONS
  doctor [--fix]        delegate to jdp doctor (CVE + outdated + score)
  deps                  delegate to jdp list
```

## A @Page in Java

```java
@Page("/")
public class TasksPage extends Component {

    @Fetch(url = "/api/tasks", empty = "No tasks yet")
    Var<List<TaskModel>> tasks;

    @Action(method = "POST", url = "/api/tasks")            void createTask() {}
    @Action(method = "PUT",  url = "/api/tasks/{id}",
            body = "{ ...t, done: !t.done }")               void toggle(TaskModel t) {}
    @Action(method = "DELETE", url = "/api/tasks/{id}")     void remove(Long id) {}

    @Override
    public Element render() {
        return div().className("max-w-2xl mx-auto p-8").children(
            h1("Tasks").className("text-2xl font-bold mb-6"),
            form().className("flex gap-2 mb-6").onSubmit("createTask").children(
                input().className("flex-1 px-3 py-2 border rounded")
                       .placeholder("New task...").attr("name", "title"),
                button("Add").type("submit")
                       .className("px-4 py-2 bg-blue-500 text-white rounded")
            ),
            ul(each("tasks", "t",
                li().key($("t.id")).className("flex items-center gap-3 py-2 border-b").children(
                    input().type("checkbox").checked($("t.done"))
                           .onChange($("() => toggle(t)")),
                    span().classIf($("t.done"), "line-through text-gray-400", "")
                          .text($("t.title")),
                    button("Delete").onClick($("() => remove(t.id)"))
                                    .className("ml-auto text-red-500 text-sm")
                )
            ))
        );
    }
}
```

`macc codegen` emits a complete `TasksPage.tsx` with `useState`,
`useEffect`, `fetch`, and the three action handlers — no TypeScript
written by hand.

## UI components (shadcn-style)

```bash
macc add button card dialog
```

This:

1. Downloads each component's TSX into `src/main/frontend/src/components/ui/`
   (using shadcn's own CLI — initializes `components.json` automatically the first time).
2. Scans the TSX for exports.
3. Writes a **Java wrapper** at `src/main/java/<base>/ui/external/<Pascal>.java`:

```java
public final class Button {
    private static final String SRC = "@/components/ui/button";
    public static final ExternalComponent button     = External.from(SRC, "Button");
    public static final ExternalComponent buttonVariants = External.from(SRC, "buttonVariants");
}
```

Use it from a @Page just like any tag:

```java
import com.acme.ui.external.Button;

div().children(
    Button.button.className("bg-blue-500").children(span("Save"))
)
```

The TSX file is **yours** — edit colors, layout, behavior. The Java
wrapper only describes the public surface (what you can call from Java).
Re-run `macc add <comp>` to update the wrapper if exports change.

## Templates (your own reusable DSL components)

`macc g template UserCard` scaffolds:

```java
@Component
public class UserCard extends Component {
    @Prop String name;
    @Prop String email;
    @Prop @Slot Element actions;

    @Override
    public Element render() {
        return div().className("rounded shadow p-4").children(
            h2(name),
            p(email).className("text-gray-500"),
            div().className("flex gap-2 mt-2").children(actions)
        );
    }
}
```

Reuse anywhere by calling `render(UserCard.class)` or composing
directly in another `@Page`.

## DSL primitives

| Java | Emits |
|---|---|
| `div().className("p-4").children(...)` | `<div className="p-4">…</div>` |
| `h1("Title")` | `<h1>Title</h1>` |
| `each("items", "x", li().key($("x.id")).text($("x.name")))` | `{items.map(x => <li key={x.id}>{x.name}</li>)}` |
| `classIf(cond, "yes", "no")` | `cond ? "yes" : "no"` |
| `pick(cond, a, b)` | `cond ? a : b` |
| `group(a, b, c)` | `<>a b c</>` |
| `$("expr")` | `{expr}` (escape hatch) |
| `@Page("/path")` | route entry in `routes.tsx` |
| `@Model record Foo(...)` | `interface Foo` in `types/Foo.ts` |
| `@State int count` | `useState<number>(0)` |
| `@Fetch(url, empty)` | `useState + useEffect + fetch` + auto Spinner/Error/Empty |
| `@Action(method, url, body)` | TS handler function with `fetch` |
| `External.from(pkg, name)` | `import { name } from "pkg"` + `<name />` |

## How it works

```
src/main/java/.../ui/*.java        ◄── you write
src/main/frontend/components/ui/   ◄── shadcn TSX (yours to edit)
        │
        │  macc codegen
        ▼
src/main/frontend/
  ├── pages/*.tsx                   ◄── generated from @Page
  ├── types/*.ts                    ◄── generated from @Model
  ├── components/StateViews.tsx     ◄── generated (Spinner/Error/Empty)
  └── routes.tsx                    ◄── generated route table
        │
        │  vite build
        ▼
src/main/resources/static/         ◄── bundled by `macc install`
        │
        │  mvn spring-boot:run
        ▼
http://localhost:8080/              ◄── one server, frontend + backend
```

## License

MIT — Fernando Bezerra · 2026
