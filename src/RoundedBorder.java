import java.awt.*;

class RoundedBorder implements javax.swing.border.Border {
    private int radius;
    
    public RoundedBorder(int radius) { 
        this.radius = radius; 
    }
    
    public Insets getBorderInsets(Component c) { 
        return new Insets(radius+2, radius+8, radius+2, radius+8); 
    }
    
    public boolean isBorderOpaque() { 
        return false; 
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(200, 200, 200));
        g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
    }
}