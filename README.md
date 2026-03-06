# gavrog
Generation, analysis and visualization of reticular ornaments using Gavrog.

This is the Java code for the programs Systre and 3dt, and associated libraries.

The Java version of Systre will continue to be maintained indefinitely, but it is unlikely that significant new features will be added.

Unfortunately, 3dt is a casualty of 64-bit Java, among other things. It ended up being too hard to maintain, and I decided instead to focus on my new project webGavrog. It is not yet at the point where it can completely replace 3dt, but I think it's approaching usefulness. Unlike 3dt, it can display nets directly, not just in association with tilings.

See https://github.com/odf/webGavrog for the code and http://gavrog.org/webGavrog/ for an online version. A file with all applicable RCSR nets can be downloaded from http://rcsr.net/systre as "Systre input data (.cgd)" and then opened in webGavrog.

An online version of Systre as part of webGavrog is not yet available, but there is a command line script implementing most of its functionality.


## 3dt and 64-bit status

The hardest migration issue for 3dt is the historical OpenGL stack (jReality + JOGL 1.1.1 native libraries), which was packaged for older platforms and does not map cleanly to modern 64-bit JVM setups.

This repository now includes a short-term compatibility step: launch scripts disable the OpenGL backend by default and 3dt falls back to jReality's software viewer when OpenGL initialization fails.

A detailed migration plan is documented in `docs-3dt-64bit-migration-plan.md`.
