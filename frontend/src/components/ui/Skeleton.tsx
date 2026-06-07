export function Skeleton({ className = '' }: { className?: string }) {
  return (
    <div
      className={`animate-pulse bg-apple-border/50 rounded-xl ${className}`}
    />
  );
}

export function CardSkeleton() {
  return (
    <div className="border border-apple-border rounded-xl p-5">
      <Skeleton className="h-5 w-3/4 mb-3" />
      <Skeleton className="h-4 w-full mb-2" />
      <Skeleton className="h-4 w-2/3 mb-4" />
      <div className="flex items-center justify-between mt-4 pt-3 border-t border-apple-border">
        <Skeleton className="h-3 w-24" />
        <Skeleton className="h-3 w-12" />
      </div>
    </div>
  );
}

export function BoardColumnSkeleton() {
  return (
    <div className="flex-1 min-w-[250px] flex flex-col">
      <div className="rounded-t-xl px-3 py-2 bg-gray-100">
        <Skeleton className="h-4 w-20" />
      </div>
      <div className="flex-1 p-2 rounded-b-xl bg-gray-100">
        {[1, 2, 3].map((i) => (
          <div key={i} className="bg-white rounded-lg p-3 mb-2 border border-apple-border">
            <Skeleton className="h-4 w-3/4 mb-2" />
            <Skeleton className="h-3 w-full" />
          </div>
        ))}
      </div>
    </div>
  );
}
