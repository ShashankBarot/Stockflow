import Link from "next/link";

export default function RegisterPage() {
  return <main className="mx-auto w-full max-w-[400px]"><p className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">Account access</p><h1 className="mt-2 text-[25px] font-medium tracking-tight">Staff accounts are provisioned by an administrator</h1><p className="mt-2 text-sm text-muted-foreground">Ask your Stockroom administrator to create your account and assign the appropriate role.</p><Link href="/login" className="mt-6 inline-flex h-9 items-center rounded-md bg-foreground px-3 text-sm font-medium text-background">Return to sign in</Link></main>;
}
