# Contributing to Scala Spark Distributed Data Pipeline

Thank you for considering contributing to this project! We welcome contributions from the community.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Testing](#testing)
- [Code Style](#code-style)
- [Pull Request Process](#pull-request-process)
- [Reporting Bugs](#reporting-bugs)
- [Feature Requests](#feature-requests)

## Code of Conduct

This project adheres to a code of conduct that all contributors are expected to follow. By participating, you are expected to uphold this code:

- Be respectful and inclusive
- Be collaborative and supportive
- Focus on what is best for the community
- Show empathy towards other community members

## Getting Started

### Prerequisites

- JDK 11 or higher
- SBT 1.9.x
- Scala 2.12.15
- Apache Spark 3.5.0 (for local testing)

### Setup Development Environment

1. **Fork the repository** to your GitHub account

2. **Clone your fork**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/scala-spark-distributed-data-pipeline.git
   cd scala-spark-distributed-data-pipeline
   ```

3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/galafis/scala-spark-distributed-data-pipeline.git
   ```

4. **Install dependencies**:
   ```bash
   sbt update
   ```

5. **Verify setup**:
   ```bash
   sbt compile
   sbt test
   ```

## Development Workflow

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**:
   - Write clean, readable code
   - Follow the existing code style
   - Add tests for new functionality
   - Update documentation as needed

3. **Commit your changes**:
   ```bash
   git add .
   git commit -m "feat: Add your feature description"
   ```

   We follow [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat:` - New feature
   - `fix:` - Bug fix
   - `docs:` - Documentation changes
   - `test:` - Adding or updating tests
   - `refactor:` - Code refactoring
   - `chore:` - Maintenance tasks

4. **Keep your branch updated**:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

5. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

## Testing

We maintain high standards for code quality and test coverage.

### Running Tests

```bash
# Run all tests
sbt test

# Run specific test
sbt "testOnly pipeline.DataPipelineTest"

# Run tests with coverage
sbt coverage test coverageReport

# View coverage report
open target/scala-2.12/scoverage-report/index.html
```

### Writing Tests

- All new features must include unit tests
- Tests should be comprehensive and cover edge cases
- Use descriptive test names
- Follow the existing test structure in `src/test/scala`

Example test:
```scala
test("transformData should remove duplicates") {
  val testData = Seq(
    ("Order1", "2025-01-15", "Electronics", 100.0),
    ("Order1", "2025-01-15", "Electronics", 100.0)
  ).toDF("Order ID", "Order Date", "Category", "Sales")
  
  val result = DataPipeline.transformData(testData)
  
  result.count() should be(1)
}
```

## Code Style

We use `scalafmt` for consistent code formatting.

### Format Your Code

```bash
# Check formatting
sbt scalafmtCheckAll

# Format code
sbt scalafmtAll
```

### Code Guidelines

- **Naming Conventions**:
  - Classes and objects: `PascalCase`
  - Methods and variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`

- **Documentation**:
  - Add ScalaDoc comments for public APIs
  - Include parameter descriptions
  - Document return types and exceptions

- **Best Practices**:
  - Keep methods small and focused
  - Avoid deeply nested code
  - Use meaningful variable names
  - Prefer immutability
  - Handle errors gracefully

## Pull Request Process

1. **Ensure all tests pass**:
   ```bash
   sbt clean test
   ```

2. **Verify code formatting**:
   ```bash
   sbt scalafmtCheckAll
   ```

3. **Update documentation**:
   - Update README.md if needed
   - Add inline comments for complex logic
   - Update relevant ScalaDoc

4. **Create Pull Request**:
   - Use a descriptive title
   - Reference related issues
   - Provide detailed description of changes
   - Include screenshots for UI changes

5. **PR Template**:
   ```markdown
   ## Description
   Brief description of the changes
   
   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update
   
   ## Testing
   Describe the tests you ran
   
   ## Checklist
   - [ ] My code follows the code style
   - [ ] I have added tests
   - [ ] All tests pass
   - [ ] I have updated documentation
   ```

6. **Review Process**:
   - Wait for maintainer review
   - Address feedback promptly
   - Update PR based on comments
   - Once approved, maintainer will merge

## Reporting Bugs

Before submitting a bug report:
- Check if the bug has already been reported
- Verify the bug on the latest version

### Bug Report Template

```markdown
**Description**
Clear description of the bug

**To Reproduce**
Steps to reproduce the behavior:
1. Execute command '...'
2. With input data '...'
3. See error

**Expected Behavior**
What you expected to happen

**Environment**
- OS: [e.g., Ubuntu 20.04]
- Java Version: [e.g., 11.0.12]
- Scala Version: [e.g., 2.12.15]
- Spark Version: [e.g., 3.5.0]

**Additional Context**
Any other relevant information
```

## Feature Requests

We welcome feature requests! Please:
- Check if the feature has been requested
- Provide clear use case
- Explain expected behavior
- Consider implementation approach

### Feature Request Template

```markdown
**Is your feature request related to a problem?**
Clear description of the problem

**Describe the solution you'd like**
Clear description of what you want

**Describe alternatives you've considered**
Other solutions you've considered

**Additional context**
Screenshots, mockups, or examples
```

## Questions?

If you have questions:
- Check the [README.md](README.md)
- Open a GitHub Discussion
- Contact the maintainers

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing! 🎉
