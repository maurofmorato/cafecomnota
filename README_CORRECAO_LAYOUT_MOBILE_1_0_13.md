# Café com nota - 1.0.13 - ajuste de layout no celular real

## O que mudou

- Redução de fontes e espaçamentos gerais em componentes reutilizados.
- Cards da Home ficaram mais compactos.
- Cards de ranking/busca/top cafés agora usam layout vertical, evitando que preço e nomes longos briguem pela mesma linha.
- Nome do café limitado a 2 linhas.
- Marca limitada a 2 linhas.
- Tipo/torra limitado a 1 linha.
- Preço por kg passou para linha própria dentro do card.
- Chips do Ranking foram reorganizados para não ficarem apertados.
- Tela Dar nota usa opções de preparo em coluna, eliminando o risco de “Espresso” quebrar letra por letra.
- Conteúdo ganhou margem inferior maior para não ficar escondido atrás da barra inferior.
- Home mostra provisoriamente `teste 1.0.13` no chip de origem dos dados.
- `versionCode` atualizado para 7 e `versionName` para 1.0.13.

## Arquivos alterados

- app/build.gradle.kts
- app/src/main/java/com/maurofmorato/cafecomnota/ui/components/CafeComponents.kt
- app/src/main/java/com/maurofmorato/cafecomnota/ui/components/ResponsiveComponents.kt
- app/src/main/java/com/maurofmorato/cafecomnota/ui/screens/HomeScreen.kt
- app/src/main/java/com/maurofmorato/cafecomnota/ui/screens/RankingScreen.kt
- app/src/main/java/com/maurofmorato/cafecomnota/ui/screens/ReviewCoffeeScreen.kt

## Como aplicar

Copie/descompacte o ZIP sobre:

D:\projetos\Cafecomnota

Depois rode:

cd D:\projetos\Cafecomnota

.\gradlew.bat assembleDebug

adb install -r app\build\outputs\apk\debug\app-debug.apk

adb shell monkey -p com.maurofmorato.cafecomnota 1

## Testes sugeridos

1. Home: conferir cards de ação e chip `teste 1.0.13`.
2. Ranking: conferir que a lista aparece logo abaixo dos filtros.
3. Ranking/Search/Home Top cafés: conferir nomes longos como Black Tucano Honey Coffee.
4. Dar nota: conferir que Espresso não fica vertical.
5. Dar nota: preço 20 deve virar 20,00 ao sair do campo.

## Checkpoint Git

cd D:\projetos\Cafecomnota

git status
git add .
git commit -m "versao 1.0.13 - ajusta layout mobile real"
git push

echo "git concluído"
