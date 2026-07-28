# Café com nota 1.0.29 — responsividade e tradução

## O que mudou

- tipografia adaptativa para celulares estreitos e escalas de fonte maiores;
- títulos de seção não quebram mais no meio das palavras;
- Perfil ajustado para preservar a hierarquia do layout de referência;
- Perfil conectado traduzido em português, inglês, espanhol, francês, alemão,
  chinês e japonês;
- subtítulo, estados de avaliação e preço da Busca agora respeitam o idioma;
- pacote neutro, sem palavras, exibido nos resultados sem foto de produto;
- indicação flutuante “Mais abaixo” removida para não cobrir resultados.

## Aplicação no Windows

```powershell
cd D:\temp\cafecomnota

Expand-Archive `
  -Path .\cafecomnota_1_0_29_responsividade_traducao.zip `
  -DestinationPath D:\projetos\Cafecomnota `
  -Force

cd D:\projetos\Cafecomnota

Select-String `
  -Path .\app\build.gradle.kts `
  -Pattern 'versionCode|versionName'

.\gradlew.bat assembleDebug
```

Somente instale o APK quando aparecer `BUILD SUCCESSFUL`.

