package com.cofrete.coreapi.compliance;

import java.util.List;

final class ComplianceWording {

    static final String RNTRC_OFFICIAL_ACTION_URL =
        "https://www.gov.br/antt/pt-br/assuntos/cargas/rntrc-1/rntrc-capa";
    static final String RNTRC_PUBLIC_CONSULTATION_URL = "https://consultapublica.antt.gov.br/";
    static final String INSURANCE_OFFICIAL_ACTION_URL =
        "https://www.gov.br/susep/pt-br/central-de-conteudos/noticias/2024/setembro/publicada-nova-norma-sobre-seguros-de-responsabilidade-civil-dos-transportadores-de-carga";

    static final String RNTRC_EXPLANATION =
        "RNTRC is your official ANTT registration for paid cargo transportation in Brazil.";
    static final String RNTRC_DIGITAL_GUIDANCE =
        "To update your RNTRC, access RNTRC Digital using your gov.br account, level prata or ouro.";
    static final String RNTRC_NOT_OFFICIAL_RECORD =
        "Your Cofrete profile is not an official ANTT record. Official updates must be done through ANTT/RNTRC Digital.";
    static final String RNTRC_PUBLIC_CHECK_UNSUPPORTED =
        "Cofrete does not store gov.br credentials or update ANTT records. Use official ANTT consultation or RNTRC Digital for official status.";
    static final String INSURANCE_CAVEAT =
        "Insurance data is organization metadata. Confirm coverage, obligations, and policy validity with the insurer, SUSEP, or a qualified professional.";
    static final String INSURANCE_SOURCE_CURRENT =
        "Insurance regulatory source metadata is current for advisory reminders, but Cofrete does not certify coverage or recommend policies.";
    static final String INSURANCE_SOURCE_STALE =
        "Insurance regulatory source metadata is stale or incomplete. Keep guidance conservative and confirm coverage with SUSEP, insurer, broker, or qualified professional.";
    static final String INSURANCE_SOURCE_MISSING =
        "No source-backed insurance requirement metadata is configured for this date. Keep mandatory-insurance guidance conservative.";
    static final String DOCUMENT_CAVEAT =
        "Document reminders are advisory organization aids and do not certify legal, tax, insurance, or government compliance.";
    static final String GENERAL_CAVEAT =
        "Cofrete is not an official government, legal, tax, accounting, or insurance channel.";

    private ComplianceWording() {
    }

    static List<String> rntrcGuidance() {
        return List.of(RNTRC_EXPLANATION, RNTRC_DIGITAL_GUIDANCE, RNTRC_NOT_OFFICIAL_RECORD);
    }

    static List<String> caveats() {
        return List.of(GENERAL_CAVEAT, RNTRC_NOT_OFFICIAL_RECORD, INSURANCE_CAVEAT, DOCUMENT_CAVEAT);
    }
}
