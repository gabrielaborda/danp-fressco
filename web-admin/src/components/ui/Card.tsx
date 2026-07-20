import React from 'react';
import { cn } from '../../utils/cn';

interface CardProps {
  title?: string;
  children: React.ReactNode;
  className?: string;
}

export function Card({ title, children, className }: CardProps) {
  return (
    <div className={cn('rounded-lg border border-border bg-surface shadow-sm p-4', className)}>
      {title && <h3 className='mb-2 text-lg font-semibold text-text-primary'>{title}</h3>}
      {children}
    </div>
  );
}
