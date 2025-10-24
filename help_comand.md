# Текстовое представление всех модулей

```shell script
./gradlew generateModulesGraphvizText -Pmodules.graph.output.gv=all_modules 
```

# График всех модулей

```shell script
dot -Tpng all_modules -o all_modules.png
```