// Clean inline SVG icons (Lucide-style: 24x24, 2px stroke, round caps).
// No emojis anywhere in the UI.

function Icon({ size = 20, children, ...props }) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      {...props}
    >
      {children}
    </svg>
  )
}

export const Plus = p => <Icon {...p}><path d="M12 5v14" /><path d="M5 12h14" /></Icon>
export const Minus = p => <Icon {...p}><path d="M5 12h14" /></Icon>
export const Close = p => <Icon {...p}><path d="M18 6 6 18" /><path d="m6 6 12 12" /></Icon>
export const Bag = p => (
  <Icon {...p}>
    <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" />
    <path d="M3 6h18" />
    <path d="M16 10a4 4 0 0 1-8 0" />
  </Icon>
)
export const ArrowRight = p => <Icon {...p}><path d="M5 12h14" /><path d="m12 5 7 7-7 7" /></Icon>
export const Lock = p => (
  <Icon {...p}>
    <rect width="18" height="11" x="3" y="11" rx="2" ry="2" />
    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
  </Icon>
)
export const LogOut = p => (
  <Icon {...p}>
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
    <path d="m16 17 5-5-5-5" />
    <path d="M21 12H9" />
  </Icon>
)
export const Trash = p => (
  <Icon {...p}>
    <path d="M3 6h18" />
    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
  </Icon>
)
export const Check = p => <Icon {...p}><path d="M20 6 9 17l-5-5" /></Icon>
