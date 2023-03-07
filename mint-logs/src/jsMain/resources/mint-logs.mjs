import module from "./testmints-mint-logs.js"

const {MochaLoggingReporter} = module

export const mochaHooks = {
    beforeAll(done) {
        MochaLoggingReporter.beforeAll()
        done();
    },
    beforeEach(done) {
        MochaLoggingReporter.beforeEach()
        done();
    }
};
