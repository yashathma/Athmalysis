import SwiftUI

struct WatchlistView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @Binding var selectedTab: Int
    @State private var activeSwipeID: String?
    @State private var draggingItem: String?
    @State private var draggingFromIndex: Int?
    @State private var draggingToIndex: Int?
    @State private var dragOffset: CGFloat = 0

    private var displayStocks: [Stock] {
        viewModel.watchlistStocks.compactMap { viewModel.stockDataMap[$0] }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header
            HStack {
                Text("Watchlist")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)

                Spacer()

                Button(action: {
                    viewModel.watchlistNavPath.append(WatchlistRoute.search)
                }) {
                    Image(systemName: "plus.circle.fill")
                        .font(.title2)
                        .foregroundStyle(.gray)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)

            Spacer().frame(height: 16)

            if displayStocks.isEmpty {
                Spacer()
                HStack {
                    Spacer()
                    Text("Your watchlist is empty.\nTap + to add stocks.")
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.gray)
                    Spacer()
                }
                Spacer()
            } else {
                ScrollView {
                    VStack(spacing: 0) {
                        ForEach(Array(displayStocks.enumerated()), id: \.element.id) { index, stock in
                            VStack(spacing: 0) {
                                StockRow(
                                    stock: stock,
                                    activeSwipeID: $activeSwipeID,
                                    draggingItem: $draggingItem,
                                    isDragging: draggingItem == stock.symbol,
                                    dragOffset: draggingItem == stock.symbol ? dragOffset : 0,
                                    visualOffset: calculateVisualOffset(for: index),
                                    onStartDrag: {
                                        draggingItem = stock.symbol
                                        draggingFromIndex = index
                                        draggingToIndex = index
                                    },
                                    onDragChanged: { translation in
                                        dragOffset = translation
                                        updateTargetIndex(currentIndex: index, translation: translation)
                                    },
                                    onEndDrag: {
                                        performMove()
                                        draggingItem = nil
                                        draggingFromIndex = nil
                                        draggingToIndex = nil
                                        dragOffset = 0
                                    },
                                    onClick: {
                                        viewModel.selectedStock = stock.symbol
                                        selectedTab = 1
                                    },
                                    onRemove: {
                                        withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                                            viewModel.removeStock(stock.symbol)
                                        }
                                    }
                                )

                                if stock.id != displayStocks.last?.id {
                                    Divider()
                                        .background(Color(white: 0.2))
                                        .padding(.horizontal, 4)
                                }
                            }
                            .zIndex(draggingItem == stock.symbol ? 999 : 0)
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
        .background(Color.black)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.startAutoRefresh()
        }
        .onDisappear {
            viewModel.stopAutoRefresh()
        }
    }

    private func calculateVisualOffset(for index: Int) -> CGFloat {
        guard let fromIndex = draggingFromIndex,
              let toIndex = draggingToIndex,
              draggingItem != nil else {
            return 0
        }

        let rowHeight: CGFloat = 63 // row height + divider

        if fromIndex < toIndex {
            // Dragging down
            if index > fromIndex && index <= toIndex {
                return -rowHeight
            }
        } else if fromIndex > toIndex {
            // Dragging up
            if index < fromIndex && index >= toIndex {
                return rowHeight
            }
        }

        return 0
    }

    private func updateTargetIndex(currentIndex: Int, translation: CGFloat) {
        let rowHeight: CGFloat = 63
        let offset = translation / rowHeight
        let targetIndex = max(0, min(displayStocks.count - 1, currentIndex + Int(round(offset))))
        draggingToIndex = targetIndex
    }

    private func performMove() {
        guard let fromIndex = draggingFromIndex,
              let toIndex = draggingToIndex,
              fromIndex != toIndex else {
            return
        }

        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
            viewModel.moveStock(from: fromIndex, to: toIndex)
        }
    }
}

struct StockRow: View {
    let stock: Stock
    @Binding var activeSwipeID: String?
    @Binding var draggingItem: String?
    let isDragging: Bool
    let dragOffset: CGFloat
    let visualOffset: CGFloat
    let onStartDrag: () -> Void
    let onDragChanged: (CGFloat) -> Void
    let onEndDrag: () -> Void
    let onClick: () -> Void
    let onRemove: () -> Void

    @State private var swipeOffset: CGFloat = 0
    @GestureState private var isDetectingLongPress = false

    private let trashWidth: CGFloat = 70

    private var isRevealed: Bool {
        activeSwipeID == stock.symbol
    }

    var body: some View {
        ZStack(alignment: .trailing) {
            // Main row content
            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 3) {
                    Text(stock.symbol)
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundStyle(.white)
                    Text(stock.name)
                        .font(.caption)
                        .foregroundStyle(.gray)
                        .lineLimit(1)
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 3) {
                    Text(String(format: "$%.2f", stock.currentPrice))
                        .font(.headline)
                        .fontWeight(.semibold)
                        .foregroundStyle(.white)

                    Text("\(stock.isPositive ? "+" : "")\(String(format: "%.2f", stock.priceChange)) (\(stock.isPositive ? "+" : "")\(String(format: "%.2f", stock.percentageChange))%)")
                        .font(.caption)
                        .fontWeight(.medium)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(
                            RoundedRectangle(cornerRadius: 5)
                                .fill(stock.isPositive ? Color.green : Color.red)
                        )
                        .foregroundStyle(.white)
                }
            }
            .padding(.vertical, 12)
            .padding(.horizontal, isDragging ? 8 : 0)
            .background(
                RoundedRectangle(cornerRadius: isDragging ? 12 : 0)
                    .fill(Color.black)
            )
            .overlay(
                RoundedRectangle(cornerRadius: isDragging ? 12 : 0)
                    .stroke(Color.gray.opacity(isDragging ? 0.3 : 0), lineWidth: 1)
            )
            .scaleEffect(isDragging ? 1.08 : 1.0)
            .shadow(color: isDragging ? Color.black.opacity(0.5) : Color.clear, radius: isDragging ? 20 : 0, y: isDragging ? 10 : 0)
            .offset(x: swipeOffset)
            .offset(y: isDragging ? dragOffset : visualOffset)
            .animation(.spring(response: 0.3, dampingFraction: 0.8), value: visualOffset)
            .animation(.spring(response: 0.25, dampingFraction: 0.75), value: isDragging)
            .contentShape(Rectangle())
            .gesture(
                // Single drag gesture handles both taps and swipes
                DragGesture(minimumDistance: 0)
                    .onChanged { value in
                        guard draggingItem == nil else { return }
                        let horizontal = value.translation.width
                        let vertical = abs(value.translation.height)
                        let totalMovement = abs(horizontal) + vertical

                        // Ignore small movements (will be treated as tap on end)
                        guard totalMovement > 10 else { return }

                        // Only allow horizontal swipe if movement is primarily horizontal
                        guard vertical < 30 else { return }

                        if isRevealed {
                            let newOffset = -trashWidth + horizontal
                            swipeOffset = min(0, max(-trashWidth, newOffset))
                        } else {
                            if horizontal < 0 {
                                if activeSwipeID != nil && activeSwipeID != stock.symbol {
                                    activeSwipeID = nil
                                }
                                swipeOffset = max(-trashWidth, horizontal)
                            }
                        }
                    }
                    .onEnded { value in
                        guard draggingItem == nil else { return }
                        let horizontal = abs(value.translation.width)
                        let vertical = abs(value.translation.height)
                        let totalMovement = horizontal + vertical

                        // Tap detection: minimal movement
                        if totalMovement < 10 {
                            if isRevealed {
                                withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                                    swipeOffset = 0
                                    activeSwipeID = nil
                                }
                            } else {
                                onClick()
                            }
                            return
                        }

                        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                            if isRevealed {
                                if value.translation.width > trashWidth * 0.3 {
                                    swipeOffset = 0
                                    activeSwipeID = nil
                                } else {
                                    swipeOffset = -trashWidth
                                }
                            } else {
                                if -value.translation.width > trashWidth * 0.4 {
                                    swipeOffset = -trashWidth
                                    activeSwipeID = stock.symbol
                                } else {
                                    swipeOffset = 0
                                }
                            }
                        }
                    }
            )
            .simultaneousGesture(
                // Long press + drag gesture for reordering
                LongPressGesture(minimumDuration: 0.3)
                    .updating($isDetectingLongPress) { currentState, gestureState, _ in
                        gestureState = currentState
                    }
                    .onEnded { _ in
                        guard !isRevealed, draggingItem == nil else { return }
                        onStartDrag()
                        // Haptic feedback
                        let generator = UIImpactFeedbackGenerator(style: .medium)
                        generator.impactOccurred()
                    }
                    .sequenced(before: DragGesture())
                    .onChanged { value in
                        switch value {
                        case .second(true, let drag):
                            if let drag = drag, isDragging {
                                onDragChanged(drag.translation.height)
                            }
                        default:
                            break
                        }
                    }
                    .onEnded { _ in
                        if isDragging {
                            onEndDrag()
                        }
                    }
            )
        }
        .overlay(alignment: .trailing) {
            // Trash button as overlay so it receives taps on top of content
            if swipeOffset < 0 || isRevealed {
                Button {
                    onRemove()
                } label: {
                    ZStack {
                        Circle()
                            .fill(Color.red)
                            .frame(width: 40, height: 40)
                        Image(systemName: "trash.fill")
                            .font(.system(size: 16))
                            .foregroundStyle(.white)
                    }
                    .frame(width: trashWidth)
                    .frame(maxHeight: .infinity)
                }
                .padding(.vertical, 12)
                .transition(.move(edge: .trailing))
            }
        }
        .onChange(of: activeSwipeID) { _, newValue in
            if newValue != stock.symbol && swipeOffset != 0 {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                    swipeOffset = 0
                }
            }
        }
    }
}
