interface SimilarityBadgeProps {
    similarity: number;
}

export const SimilarityBadge = ({ similarity }: SimilarityBadgeProps) => {
    const formattedSimilarity = similarity.toFixed(1);

    let badgeClass = "px-3 py-1 text-xs font-bold rounded-full whitespace-nowrap ";

    if (similarity >= 40) {
        badgeClass += "bg-brand text-white";
    } else if (similarity >= 10) {
        badgeClass += "bg-cyan-600 text-white";
    } else {
        badgeClass += "bg-gray-200 text-secondary";
    }

    return (
        <span className={badgeClass}>
            {formattedSimilarity}%
        </span>
    );
};