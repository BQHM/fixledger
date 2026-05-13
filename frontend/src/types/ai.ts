export interface InvoiceParseResponse {
  analysisId: number;
  deviceName?: string;
  purchaseDate?: string;
  price?: number;
  seller?: string;
  suggestedCategory?: string;
}

export interface TroubleshootingResponse {
  analysisId: number;
  summary: string;
  suggestions: string[];
}

export interface MaintenanceSummaryResponse {
  analysisId: number;
  summary: string;
  careSuggestion: string;
}
