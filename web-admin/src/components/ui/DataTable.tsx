import React, { useEffect, useState } from 'react';
import { cn } from '../../utils/cn';
import { Button } from './Button';
import InputField from '../auth/login/InputField'; // reuse styled input
import { Spinner } from '../ui/Spinner'; // we'll create a simple spinner later

export type Column<T> = {
  header: string;
  accessor: keyof T | ((row: T) => React.ReactNode);
  // optional custom cell render
  render?: (value: any, row: T) => React.ReactNode;
  sortable?: boolean;
};

interface DataTableProps<T> {
  columns: Column<T>[];
  fetchData: (params: { page: number; pageSize: number; search?: string }) => Promise<{
    data: T[];
    total: number;
  }>;
  pageSizeOptions?: number[];
  searchable?: boolean;
  className?: string;
}

export function DataTable<T extends Record<string, any>>({
  columns,
  fetchData,
  pageSizeOptions = [10, 20, 50],
  searchable = true,
  className,
}: DataTableProps<T>) {
  const [data, setData] = useState<T[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(pageSizeOptions[0]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchData({ page, pageSize, search });
      setData(res.data);
      setTotal(res.total);
    } catch (e: any) {
      setError(e?.message ?? 'Error loading data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, pageSize, search]);

  const totalPages = Math.ceil(total / pageSize) || 1;

  return (
    <div className={cn('flex flex-col gap-4', className)}>
      {searchable && (
        <InputField
          type="text"
          label="Buscar"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          icon={<svg className="h-4 w-4" viewBox="0 0 24 24"><path fill="currentColor" d="M10 2a8 8 0 015.292 13.708l4.5 4.5a1 1 0 01-1.414 1.414l-4.5-4.5A8 8 0 1110 2zm0 2a6 6 0 100 12A6 6 0 0010 4z" /></svg>}
          autoComplete="off"
        />
      )}

      {error && <p className="text-error">{error}</p>}

      <div className="overflow-x-auto rounded-lg border border-border bg-surface">
        <table className="min-w-full text-sm">
          <thead className="bg-surface-variant">
            <tr>
              {columns.map((col, idx) => (
                <th key={idx} className="px-4 py-2 text-left font-medium text-text-secondary">
                  {col.header}
                </th>
              ))}
              <th className="px-4 py-2" />
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={columns.length + 1} className="p-4 text-center">
                  <Spinner size="md" />
                </td>
              </tr>
            ) : (
              data.map((row, rowIdx) => (
                <tr key={rowIdx} className={rowIdx % 2 === 0 ? 'bg-surface' : 'bg-surface-variant'}>
                  {columns.map((col, colIdx) => {
                    const value = typeof col.accessor === 'function' ? col.accessor(row) : row[col.accessor as string];
                    return (
                      <td key={colIdx} className="px-4 py-2 text-text-primary">
                        {col.render ? col.render(value, row) : value?.toString() ?? ''}
                      </td>
                    );
                  })}
                  <td className="px-4 py-2 text-right">
                    {/* actions column placeholder – caller can render extra cell via column.render */}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination controls */}
      <div className="flex items-center justify-between py-2 px-2">
        <div className="flex items-center gap-2">
          <span className="text-text-secondary text-xs">Página {page} de {totalPages}</span>
          <Button
            variant="outline"
            size="sm"
            disabled={page <= 1}
            onClick={() => setPage((p) => Math.max(p - 1, 1))}
          >
            Anterior
          </Button>
          <Button
            variant="outline"
            size="sm"
            disabled={page >= totalPages}
            onClick={() => setPage((p) => Math.min(p + 1, totalPages))}
          >
            Siguiente
          </Button>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-text-secondary text-xs">Filas por página:</span>
          <select
            value={pageSize}
            onChange={(e) => {
              setPageSize(parseInt(e.target.value, 10));
              setPage(1);
            }}
            className="rounded border border-border bg-surface px-2 py-1 text-sm text-text-primary"
          >
            {pageSizeOptions.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
}
