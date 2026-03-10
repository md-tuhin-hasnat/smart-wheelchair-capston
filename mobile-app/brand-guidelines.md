# Brand Guidelines (Meta / Modern UI Inspired)

This brand guideline defines the visual identity and UI principles for the Smart Wheelchair Android application, inspired by Meta's (Facebook/Instagram/WhatsApp) clean, modern, and accessible design systems.

## 1. Color Palette
The color system emphasizes content readability while using a distinct, recognizable primary color for interactions.

### Light Mode
- **Primary / Accent**: `#0866FF` (Vibrant Meta Blue) - Used for primary actions, active states, and focus.
- **Background**: `#F0F2F5` (Facebook Light Gray) - Used for the app boundary background to create depth.
- **Surface**: `#FFFFFF` (Solid White) - Used for cards, dialogs, and main content areas.
- **Text Primary**: `#050505` - Used for headlines and critical body text.
- **Text Secondary**: `#65676B` - Used for subtitles, timestamps, and secondary information.
- **Error / Destructive**: `#E41E3F` - Used for Delete actions.
- **Success**: `#31A24C` - Used for connected statuses.

### Dark Mode (Optional but recommended)
- **Primary / Accent**: `#2D88FF`
- **Background**: `#18191A`
- **Surface**: `#242526`
- **Text Primary**: `#E4E6EB`
- **Text Secondary**: `#B0B3B8`

## 2. Typography
A clean, sans-serif font ensures maximum legibility across different device screens. Android's native **Roboto** or Google's **Inter** font family should be used.
- **Headings (H1/H2)**: Bold (700), tight letter spacing.
- **Body**: Regular (400), 16sp or 14sp, comfortable line height (1.5x).
- **Buttons**: Medium (500), mostly sentence-case rather than ALL-CAPS, maximizing approachability.

## 3. Cards & Elevation
Using "Cards" should be deliberate, not just wrapping every element. They should group related information conceptually.
- **Corners**: Subtle and rounded, `8dp` to `12dp` (Modern Meta apps avoid sharp corners).
- **Shadow/Elevation**: Extremely subtle. Avoid heavy, muddy shadows. 
  - *Light mode shadow*: `0px 1px 2px rgba(0, 0, 0, 0.1), 0px 2px 4px rgba(0, 0, 0, 0.05)`
  - *Dark mode shadow*: Use surface color lightness instead of inner shadows, or very faint ambient shadow.
- **Borders**: Alternatively to shadows, a faint `1px` border (e.g., `#CED0D4`) on cards combined with flat surfaces provides a clean, modern aesthetic.

## 4. Spacing & Layout
Follow an **8dp / 16dp / 24dp** grid system.
- Standard screen margins: `16dp`.
- Spacing between related items: `8dp`.
- Spacing between different sections: `24dp` or `32dp`.

## 5. UI Components
- **Buttons**: Minimum `48dp` touch target height. Fully rounded corners (pill-shaped) or heavily rounded (`12dp`). Solid blue for primary actions, subtle gray for secondary actions.
- **Dropdowns / Selection**: Clean, native-feeling bottom sheets or inline dropdowns with clearly touchable rows.
- **Data Tables / Lists**: Keep rows airy with `16dp` vertical padding. Use clear dividers only where necessary to separate instances.

## 6. Accessibility
- Ensure a minimum contrast ratio of `4.5:1` for text against its background.
- Support dynamic text sizing.
- Provide clear touch feedback (Ripple effect on Android) for all interactive elements.
