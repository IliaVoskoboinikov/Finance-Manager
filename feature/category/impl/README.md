# `:feature:category:impl`

## Module dependency graph

Категории в приложении

<!--region graph-->

```mermaid
---
config:
  layout: elk
  elk:
    nodePlacementStrategy: SIMPLE
---
graph TB
    subgraph :feature
        direction TB
        subgraph :feature:category
            direction TB
            :feature:category:api[api]:::android-library
            :feature:category:impl[impl]:::android-library
        end
    end

    subgraph :core
        direction TB
        :core:data[data]:::android-library
        :core:network[network]:::android-library
        :core:domain[domain]:::android-library
        :core:uikit[uikit]:::android-library
        :core:feature-api[feature-api]:::android-library
    end

%% =======================
%% DEPENDENCIES
%% =======================
    :feature:category:impl --> :feature:category:api
    :feature:category:impl --> :core:network
    :feature:category:impl --> :core:uikit
    :feature:category:impl --> :core:data
    :feature:category:impl --> :core:domain
    :feature:category:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
    classDef jvm-library fill:#BDB2FF,stroke:#000,stroke-width:2px,color:#000;

```// Revue me>>
