import SwiftUI

struct FrequencyMatchingView: View {
    @StateObject private var viewModel = FrequencyMatchingViewModel()
    @Binding var selectedTab: Int
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                backgroundView
                dotGridOverlay
                controlSurface(geometry: geometry)
            }
        }
        .navigationBarHidden(true)
    }
    
    private var backgroundView: some View {
        LinearGradient(
            colors: [
                Color.gray.opacity(0.15),
                Color.orange.opacity(0.25),
                Color.red.opacity(0.15)
            ],
            startPoint: .top,
            endPoint: .bottom
        )
        .ignoresSafeArea()
    }
    
    private var dotGridOverlay: some View {
        Canvas { context, size in
            let spacing: CGFloat = 25
            let dotSize: CGFloat = 1.5
            
            for x in stride(from: 0, through: size.width, by: spacing) {
                for y in stride(from: 0, through: size.height, by: spacing) {
                    let rect = CGRect(x: x - dotSize/2, y: y - dotSize/2, width: dotSize, height: dotSize)
                    context.fill(
                        Path(ellipseIn: rect),
                        with: .color(Color.white.opacity(0.15))
                    )
                }
            }
        }
        .ignoresSafeArea()
    }
    
    private func controlSurface(geometry: GeometryProxy) -> some View {
        let safeArea = geometry.safeAreaInsets
        let availableHeight = geometry.size.height - safeArea.top - safeArea.bottom - 60
        let availableWidth = geometry.size.width - 20
        let controlAreaFrame = CGRect(
            x: 10,
            y: safeArea.top + 40,
            width: availableWidth,
            height: availableHeight
        )
        
        return ZStack {
            controlLabels(frame: controlAreaFrame)
            
            FrequencyControlPoint(isDragging: viewModel.isDragging)
                .position(
                    x: controlAreaFrame.minX + controlAreaFrame.width * viewModel.controlPosition.x,
                    y: controlAreaFrame.minY + controlAreaFrame.height * viewModel.controlPosition.y
                )
                .gesture(
                    DragGesture()
                        .onChanged { value in
                            if !viewModel.isDragging {
                                viewModel.startDragging()
                            }
                            
                            let newPosition = CGPoint(
                                x: value.location.x - controlAreaFrame.minX,
                                y: value.location.y - controlAreaFrame.minY
                            )
                            
                            viewModel.updateControlPosition(
                                to: newPosition,
                                in: CGSize(width: controlAreaFrame.width, height: controlAreaFrame.height)
                            )
                        }
                        .onEnded { _ in
                            viewModel.stopDragging()
                        }
                )
        }
    }
    
    private func controlLabels(frame: CGRect) -> some View {
        ZStack {
            Text("louder")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.orange)
                .position(x: frame.midX, y: frame.minY - 15)
            
            Text("softer")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.orange)
                .position(x: frame.midX, y: frame.maxY + 15)
            
            Text("bass")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.orange)
                .rotationEffect(.degrees(-90))
                .position(x: frame.minX + 25, y: frame.midY)
            
            Text("treble")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.orange)
                .rotationEffect(.degrees(90))
                .position(x: frame.maxX - 25, y: frame.midY)
        }
    }
    
}

#Preview {
    FrequencyMatchingView(selectedTab: .constant(1))
}
