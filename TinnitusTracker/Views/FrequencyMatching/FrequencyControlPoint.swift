import SwiftUI

struct FrequencyControlPoint: View {
    let isDragging: Bool
    
    var body: some View {
        ZStack {
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            Color.orange.opacity(0.8),
                            Color.red.opacity(0.6)
                        ],
                        center: .topLeading,
                        startRadius: 5,
                        endRadius: 25
                    )
                )
                .frame(width: 50, height: 50)
                .scaleEffect(isDragging ? 1.2 : 1.0)
                .shadow(
                    color: Color.orange.opacity(isDragging ? 0.6 : 0.3),
                    radius: isDragging ? 15 : 8,
                    x: 0,
                    y: isDragging ? 8 : 4
                )
            
            Circle()
                .stroke(
                    LinearGradient(
                        colors: [Color.white.opacity(0.8), Color.white.opacity(0.3)],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 2
                )
                .frame(width: 46, height: 46)
            
            Circle()
                .fill(Color.white.opacity(0.2))
                .frame(width: 20, height: 20)
                .blur(radius: 1)
                .offset(x: -8, y: -8)
        }
        .animation(.spring(response: 0.3, dampingFraction: 0.6), value: isDragging)
    }
}

#Preview {
    VStack(spacing: 50) {
        FrequencyControlPoint(isDragging: false)
        FrequencyControlPoint(isDragging: true)
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .background(
        LinearGradient(
            colors: [
                Color.gray.opacity(0.3),
                Color.orange.opacity(0.2)
            ],
            startPoint: .top,
            endPoint: .bottom
        )
    )
}