package Helper;
import java.awt.*;

public class WrapLayout extends FlowLayout {

    public WrapLayout(int align, int hgap, int vgap){
        super(align, hgap, vgap);
    }

    public Dimension preferredLayoutSize(Container target){
        return layoutSize(target, true);
    }

    public Dimension minimumLayoutSize(Container target) {
        return layoutSize(target, false);
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;

            if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

            int hgap = getHgap();
            int vgap = getVgap();

            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

            int width = 0;
            int height = 0;
            int rowWidth = 0;
            int rowHeight = 0;

            int componentCount = target.getComponentCount();
            for (int i = 0; i < componentCount; i++) {
                Component c = target.getComponent(i);
                if (!c.isVisible()) continue;

                Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();

                if (rowWidth + d.width > maxWidth && rowWidth > 0) {
                    width = Math.max(width, rowWidth);
                    height += rowHeight + vgap;
                    rowWidth = 0;
                    rowHeight = 0;
                }

                rowWidth += d.width + hgap;
                rowHeight = Math.max(rowHeight, d.height);
            }

            width = Math.max(width, rowWidth);
            height += rowHeight;
            width += insets.left + insets.right + hgap * 2;
            height += insets.top + insets.bottom + vgap * 2;

            return new Dimension(width, height);
        }
    }
}
