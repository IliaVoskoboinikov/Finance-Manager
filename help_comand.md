# GraphStatistics кол во модулей и тд

```shell script
./gradlew generateModulesGraphStatistics
```

# Текстовое представление всех модулей
```shell script
./gradlew generateModulesGraphvizText -Pmodules.graph.output.gv=all_modules 
```

# График всех модулей
```shell script
dot -Tpng all_modules -o all_modules.png
```

# Запустить detekt

```shell script
./gradlew detekt
./gradlew detektBaseline
```

# Сгенерить файл сборки app/build/reports/buildTimeTracker

```shell script
./gradlew app:assembleDebug
```

# Ktlint

# Запустить проверку

```shell script
./gradlew --continue ktlintCheck
```

# Выполнить форматирование

```shell script
./gradlew --continue ktlintFormat 
```