# Assinatura de lançamento — Café com nota 1.0.17

Este pacote configura a assinatura de lançamento sem registrar senhas ou chaves no Git.

Após extrair o pacote na raiz do projeto, crie o arquivo `keystore.properties` na raiz de `D:\projetos\Cafecomnota`, usando `keystore.properties.example` como modelo. Preencha a senha criada no `keytool` nos dois campos de senha.

O arquivo `cafecomnota-release.jks` deve permanecer na raiz do projeto e deve receber uma cópia de segurança externa. O arquivo `.jks` e `keystore.properties` são ignorados pelo Git.

Para gerar o Android App Bundle assinado:

```powershell
.\gradlew.bat bundleRelease
```

O AAB será criado em:

```text
app\build\outputs\bundle\release\app-release.aab
```
