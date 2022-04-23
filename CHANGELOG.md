0.8.0 (released - April 2022)
------------------
The major update in this release is the transition of Thrifty to the latest 3.0.0

- `thrifty-compiler` and `thrifty-runtime` version updated to `3.0.0`
- Following Arguments were updated to compatible with the new version
- `--generated-annotation-type` is no longer supported by the `thrifty-compiler`
- `--lang=java` was added to generate the java source (Thrifty generates Kotlin code by default)
- All unittests related to `@Generated` annotations has been removed