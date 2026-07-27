import React from 'react';
import { cn } from '../../utils/cn';

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  title?: string;
  children: React.ReactNode;
  className?: string;
}

export function Card({ title, children, className, ...props }: CardProps) {
  return (
    <div className={cn('rounded-lg border border-border bg-surface shadow-sm p-4', className)} {...props}>
      {title && <h3 className='mb-2 text-lg font-semibold text-text-primary'>{title}</h3>}
      {children}
    </div>
  );
}

