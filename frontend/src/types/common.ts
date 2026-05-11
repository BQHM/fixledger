export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
  records: T[];
}

export interface PageQuery {
  pageNum?: number;
  pageSize?: number;
}

export interface SelectOption {
  label: string;
  value: string;
}