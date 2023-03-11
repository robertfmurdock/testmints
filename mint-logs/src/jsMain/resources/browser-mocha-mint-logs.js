const module = require("./testmints-mint-logs.js")
const {MochaLoggingReporter} = module

mocha.setup({
    rootHooks: {
        beforeAll(done) {
            MochaLoggingReporter.beforeAll()
            done();
        },
        beforeEach(done) {
            MochaLoggingReporter.beforeEach()
            done();
        }
    }
})
