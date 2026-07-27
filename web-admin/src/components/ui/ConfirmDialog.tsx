import { Modal } from './Modal';
import { Button } from './Button';
type ConfirmDialogProps = {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title?: string;
  description?: string;
  confirmText?: string;
  cancelText?: string;
};

export function ConfirmDialog({
  isOpen,
  onClose,
  onConfirm,
  title = 'Confirmación',
  description,
  confirmText = 'Confirmar',
  cancelText = 'Cancelar',
}: ConfirmDialogProps) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title} className="max-w-sm">
      {description && <p className="mb-4 text-text-primary">{description}</p>}
      <div className="flex justify-end gap-2">
        <Button variant="outline" size="sm" onClick={onClose}>
          {cancelText}
        </Button>
        <Button variant="danger" size="sm" onClick={onConfirm}>
          {confirmText}
        </Button>
      </div>
    </Modal>
  );
}
