package com.cs407.tickertock.data

/**
 * Fake data for War Room testing
 * This allows testing the UI without needing real stock data or API calls
 *
 * TO SWITCH BACK TO REAL DATA:
 * 1. In WarRoomScreen.kt, change useFakeData = false in the navigation route
 * 2. In NewsScreen.kt, ensure the War Room button passes real stock data
 */
object FakeWarRoomData {

    /**
     * Generate fake stock data for testing
     */
    fun getFakeStock(): Stock {
        return Stock(
            symbol = "NVDA",
            name = "NVIDIA Corporation",
            currentPrice = 195.21,
            priceChange = -3.48,
            percentageChange = -1.75
        )
    }

    /**
     * Generate fake news articles for testing
     */
    fun getFakeNewsArticles(): List<NewsArticle> {
        return listOf(
            NewsArticle(
                id = "1",
                title = "Uber CEO Dara Khosrowshahi Discusses Q4 Earnings and Future Growth",
                summary = "CEO highlighted strong Q4 performance with revenue growth exceeding expectations. The company announced plans to expand into new markets and invest heavily in autonomous vehicle technology.",
                publishedAt = "2 hours ago",
                publisher = "Tech News Daily",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "2",
                title = "Jensen Huang Warns About Rising Competition in AI Chip Market",
                summary = "NVIDIA CEO expressed concerns about increasing competition from AMD and Intel in the AI accelerator space. Despite challenges, the company maintains its market leadership position.",
                publishedAt = "2 hours ago",
                publisher = "Financial Times",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "3",
                title = "Tech Giant Announces Major Partnership Deal",
                summary = "The company has formed a strategic partnership with leading cloud providers to enhance AI infrastructure capabilities. This move is expected to boost revenue in the coming quarters.",
                publishedAt = "3 hours ago",
                publisher = "Bloomberg",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "4",
                title = "Analyst Upgrades Stock Rating Citing Strong Fundamentals",
                summary = "Major investment firm raised price target following impressive earnings report. Analysts cite strong demand for AI products and expanding market share as key drivers.",
                publishedAt = "5 hours ago",
                publisher = "MarketWatch",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "5",
                title = "New Product Launch Expected to Drive Revenue Growth",
                summary = "Company unveils next-generation technology platform aimed at enterprise customers. Early feedback suggests strong market demand and competitive positioning.",
                publishedAt = "1 day ago",
                publisher = "Reuters",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "6",
                title = "Stock Surges on Positive Sentiment About Industry Trends",
                summary = "Shares rallied following industry report showing accelerated adoption of AI technologies. Investors remain optimistic about long-term growth prospects.",
                publishedAt = "1 day ago",
                publisher = "CNBC",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "7",
                title = "Company Beats Earnings Expectations for Fifth Consecutive Quarter",
                summary = "Strong performance across all business segments drove results above analyst estimates. Management raised full-year guidance citing robust demand.",
                publishedAt = "2 days ago",
                publisher = "WSJ",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "8",
                title = "AI Chip Demand Reaches All-Time High in Data Center Market",
                summary = "Industry report shows unprecedented demand for AI accelerators. Major tech companies are placing large orders to support expanding AI infrastructure needs.",
                publishedAt = "3 days ago",
                publisher = "TechCrunch",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "9",
                title = "Supply Chain Concerns Ease as Production Ramps Up",
                summary = "Company successfully addresses previous bottlenecks with improved manufacturing capacity. Analysts expect steady product availability going forward.",
                publishedAt = "3 days ago",
                publisher = "Forbes",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "10",
                title = "New AI Framework Optimizes Performance on Latest Architecture",
                summary = "Software update delivers significant performance improvements for machine learning workloads. Developers report faster training times and better efficiency.",
                publishedAt = "4 days ago",
                publisher = "VentureBeat",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "11",
                title = "Gaming Division Shows Resilience Despite Market Headwinds",
                summary = "Consumer segment maintains strong sales momentum with new GPU launches. Pre-orders exceed expectations for next-generation gaming products.",
                publishedAt = "4 days ago",
                publisher = "IGN Business",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "12",
                title = "Institutional Investors Increase Stakes Following Strategic Announcements",
                summary = "Major hedge funds and pension funds boost positions after company unveils long-term growth strategy. Institutional ownership reaches new record levels.",
                publishedAt = "5 days ago",
                publisher = "Barron's",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "13",
                title = "Automotive Partnership Expands Self-Driving Technology Reach",
                summary = "Leading automakers adopt platform for autonomous vehicle development. New contracts expected to generate substantial recurring revenue streams.",
                publishedAt = "5 days ago",
                publisher = "Automotive News",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "14",
                title = "Research Team Achieves Breakthrough in Energy Efficiency",
                summary = "Engineers develop innovative cooling solution that reduces power consumption by 30%. Technology expected to be integrated into future product lines.",
                publishedAt = "6 days ago",
                publisher = "MIT Technology Review",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "15",
                title = "Cloud Service Providers Commit to Multi-Year Infrastructure Investments",
                summary = "Major cloud platforms announce plans to deploy thousands of AI accelerators. Long-term contracts provide strong revenue visibility.",
                publishedAt = "1 week ago",
                publisher = "Cloud Computing Today",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "16",
                title = "Cybersecurity Division Launches New Data Protection Solutions",
                summary = "Company enters growing cybersecurity market with hardware-accelerated security products. Early customer feedback highlights significant performance advantages.",
                publishedAt = "1 week ago",
                publisher = "Security Weekly",
                stockSymbol = "NVDA"
            ),
            NewsArticle(
                id = "17",
                title = "Quarterly Dividend Raised Following Strong Cash Flow Generation",
                summary = "Board approves 10% dividend increase reflecting confidence in business fundamentals. Share buyback program also expanded by $5 billion.",
                publishedAt = "1 week ago",
                publisher = "Seeking Alpha",
                stockSymbol = "NVDA"
            )
        )
    }

    /**
     * Generate complete fake war room analysis
     */
    fun getFakeWarRoomAnalysis(): WarRoomAnalysis {
        val fakeStock = getFakeStock()
        val fakeArticles = getFakeNewsArticles()
        return WarRoomAnalysis.generate(fakeStock, fakeArticles)
    }
}
