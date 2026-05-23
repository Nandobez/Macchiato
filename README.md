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
for dependency hygiene.

## Install

```bash
curl -fsSL https://raw.githubusercontent.com/Nandobez/Macchiato/main/install.sh | bash
```

Prerequisites: **JDK 17+**, **mvn**, **git**, **Node 18+** (only when
running codegen + vite build).

## 90-second tour

```bash
xpresso new tasks-app --group io.demo                      # 1) backend
cd tasks-app
xpresso g resource Task title:string done:bool             # 2) JPA CRUD
                                                           # 3) write a @Page in Java
macc new src/main/frontend                                 # 4) scaffold Vite + Tailwind
macc install                                               # 5) Java → TSX + vite build
mvn spring-boot:run                                        # 6) one server, frontend included
```

Or run both backend + frontend in dev mode together:

```bash
macc serve   # backend on :8080, vite on :5173 with /api proxy
```

## A @Page in Java

```java
package io.demo.tasksapp.ui;

import dev.nandobez.macc.dsl.*;
import dev.nandobez.macc.dsl.annotations.*;
import static dev.nandobez.macc.dsl.Tags.*;
import static dev.nandobez.macc.dsl.Helpers.*;

import java.util.List;

@Page("/")
public class TasksPage extends Component {

    @Fetch(url = "/api/tasks", empty = "No tasks yet")
    Var<List<TaskModel>> tasks;

    @Action(method = "POST", url = "/api/tasks")
    void createTask() {}

    @Action(method = "PUT", url = "/api/tasks/{id}", body = "{ ...t, done: !t.done }")
    void toggle(TaskModel t) {}

    @Action(method = "DELETE", url = "/api/tasks/{id}")
    void remove(Long id) {}

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

## Commands

```
PROJECT
  new <dir>            scaffold Vite + React + Tailwind into <dir>
  g page <Name>        generate a @Page class
  g component <Name>   generate a @Component class
  g model <Name>       generate a @Model record

CODEGEN
  codegen              scan @Page/@Model on the classpath → emit .tsx + types + routes
  install              mvn compile + codegen + npm install + vite build (one shot)

LIFECYCLE
  dev                  codegen + vite dev server (hot reload)
  serve                backend (xpresso s) + vite dev together · /api proxy

INTEGRATION
  doctor               delegate to jdp doctor (CVE + outdated + score)
  deps                 delegate to jdp list
```

## DSL surface

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

## How it works

```
src/main/java/.../ui/*.java        ◄── you write
        │
        │  macc codegen
        ▼
src/main/frontend/
  ├── pages/*.tsx                   ◄── generated
  ├── types/*.ts                    ◄── generated
  ├── components/StateViews.tsx     ◄── generated
  └── routes.tsx                    ◄── generated
        │
        │  vite build
        ▼
src/main/resources/static/         ◄── bundled by macc install
        │
        │  mvn spring-boot:run
        ▼
http://localhost:8080/              ◄── one server, frontend + backend
```

## License

MIT — Fernando Bezerra · 2026
