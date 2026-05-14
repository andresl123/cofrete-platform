# Official Source Register

Last full-register check: 2026-05-11.
ROU-220 RNTRC Digital source check: 2026-05-14.
ROU-247 ANTT freight-floor source check: 2026-05-14.
ROU-250 RNTRC public-status automation evaluation: 2026-05-14.
ROU-251 insurance regulatory source-refresh review: 2026-05-14.

Cofrete is not an official government, legal, tax, accounting, or insurance channel. Product behavior must link users back to official sources when a decision can affect compliance, tax, insurance, or legal obligations.

| Topic | Official Source | URL | Product Use |
|---|---|---|---|
| RNTRC / ANTT | ANTT RNTRC page | https://www.gov.br/antt/pt-br/assuntos/cargas/rntrc-1/rntrc-capa | RNTRC explanatory text and official update link |
| RNTRC Digital updates | ANTT Como obter e atualizar | https://www.gov.br/antt/pt-br/assuntos/cargas/rntrc-1/como-obter-e-atualizar | RNTRC Digital/gov.br prata-ou-ouro guidance and official update caveat |
| RNTRC consultation | ANTT Consulta Publica RNTRC | https://consultapublica.antt.gov.br/ | Public status check links and advisory wording |
| Road cargo transport / CIOT / VPO | ANTT cargo transport overview | https://www.gov.br/antt/pt-br/a-antt/o-transporte-de-cargas | Compliance center wording and transport concepts |
| CIOT | ANTT CIOT | https://www.gov.br/antt/pt-br/assuntos/cargas/pagamento-eletronico-de-fretes-pef-ciot/ciot | Pre-trip checklist and CIOT warning copy |
| CIOT enforcement / freight floor | ANTT CIOT and minimum freight floor news | https://www.gov.br/antt/pt-br/assuntos/ultimas-noticias/frete-irregular-e-barrado-antes-de-existir-antt-transforma-ciot-em-filtro-obrigatorio-e-reforca-o-cumprimento-do-piso-minimo | Freight-floor risk wording |
| Minimum freight floor hub | ANTT Piso Minimo do Frete | https://www.gov.br/antt/pt-br/assuntos/cargas/pagamento-eletronico-de-fretes-pef-ciot/piso-minimo-do-frete | Freight-floor source discovery and latest official publication links |
| Minimum freight floor updates | ANTT freight floor update news | https://www.gov.br/antt/pt-br/assuntos/ultimas-noticias/antt-atualiza-tabela-dos-pisos-minimos-de-frete-em-decorrencia-da-variacao-no-preco-do-diesel-s10 | Freight-floor table source discovery |
| Minimum freight floor calculator | ANTT freight calculator | https://calculadorafrete.antt.gov.br | User-facing official calculation cross-check, not bulk import source unless ANTT exposes stable machine-readable data |
| Vale-Pedagio | ANTT Vale-Pedagio Obrigatorio | https://www.gov.br/antt/pt-br/assuntos/cargas/vale-pedagio-obrigatorio/o-que-e | Toll pass-through and reimbursement rules |
| Loading/unloading | ANTT Carga e Descarga | https://www.gov.br/antt/pt-br/assuntos/cargas/carga-descarga | Waiting-time rule source review |
| Toll plazas | ANTT Dados Abertos - Praca de Pedagio | https://dados.antt.gov.br/dataset/praca-de-pedagio | Toll plaza importer source |
| ANP fuel prices | ANP latest fuel price surveys | https://www.gov.br/anp/pt-br/assuntos/precos-e-defesa-da-concorrencia/precos/levantamento-de-precos-de-combustiveis-ultimas-semanas-pesquisadas | Diesel price importer source discovery |
| ANP open fuel history | ANP historical fuel price data | https://www.gov.br/anp/pt-br/centrais-de-conteudo/dados-abertos/serie-historica-de-precos-de-combustiveis | Fixture/import format and freshness |
| Insurance | SUSEP RC transport cargo news | https://www.gov.br/susep/pt-br/central-de-conteudos/noticias/2024/setembro/publicada-nova-norma-sobre-seguros-de-responsabilidade-civil-dos-transportadores-de-carga | Insurance metadata and advisory copy |
| Insurance update | SUSEP 2026 RC-V update | https://www.gov.br/susep/pt-br/central-de-conteudos/noticias/2026/marco/cnsp-altera-norma-sobre-seguro-obrigatorio-de-responsabilidade-civil-para-transporte-rodoviario-de-cargas | Risk register and policy wording review |
| MEI Caminhoneiro | Portal do Empreendedor | https://www.gov.br/empresas-e-negocios/pt-br/empreendedor/mei-caminhoneiro/mei-caminhoneiro-3 | MEI limit monitor source |
| Tax authority | Receita Federal | https://www.gov.br/receitafederal/pt-br | Tax profile disclaimers and official referral |
| Individual income tax manual | Receita Federal IR manual | https://www.gov.br/receitafederal/pt-br/assuntos/meu-imposto-de-renda/preenchimento/manual-mir/rendimentos/rendimentos-do-trabalho | Pessoa Fisica autonoma planning-source review |
| State vehicle obligations | State DETRAN/SEFAZ portals | State-specific official URLs | IPVA/licensing reminders |
| IPVA competency | gov.br IPVA state-tax note | https://www.gov.br/secom/pt-br/fatos/brasil-contra-fake/noticias/2023/12/ipva-e-imposto-de-competencia-estadual | IPVA state-specific rule warning |
| Pix | Banco Central Pix | https://www.bcb.gov.br/estabilidadefinanceira/pix | Future payment reconciliation planning |

## Review Rule

Re-check this register before launch, before changing compliance copy, and whenever implementation issues automate source ingestion or status lookup.

## ROU-250/ROU-251 Review Notes

- RNTRC Digital remains an official update path requiring gov.br authentication; Cofrete must keep app login separate and must not handle gov.br credentials.
- ANTT public RNTRC consultation stays link-only for MVP because automation terms, source stability, and anti-abuse behavior are not approved.
- SUSEP/insurance source guidance is represented as source-review metadata with reviewed timestamp, source URL, freshness, and confidence. Driver-entered policy data remains separate from official/professional guidance.
